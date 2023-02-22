package checkmate.goal.application;

import checkmate.common.cache.CacheHandler;
import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.domain.*;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

import static checkmate.notification.domain.NotificationType.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamMateCommandService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final TeamMateRepository teamMateRepository;
    private final NotificationRepository notificationRepository;
    private final TeamMateInitiateManager teamMateInitiateManager;
    private final CacheHandler cacheHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final TeamMateCommandMapper mapper;

    @Transactional
    public void inviteTeamMate(TeamMateInviteCommand command) {
        TeamMate invitee = findOrCreateInvitee(command.goalId(), command.inviteeNickname());
        invitee.toWaitingStatus();
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_GOAL,
                getInviteGoalNotificationDto(command, invitee.getUserId())));
    }

    @Caching(evict = {
            @CacheEvict(
                    value = CacheKey.ONGOING_GOALS,
                    key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}"),
            @CacheEvict(value =
                    CacheKey.TODAY_GOALS,
                    key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}")
    })
    @Transactional
    public TeamMateAcceptResult inviteAccept(TeamMateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        TeamMate teamMate = applyToTeamMate(notification.getLongAttribute("teamMateId"),
                teamMateInitiateManager::initiate);

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_ACCEPT,
                getInviteAcceptNotificationDto(teamMate, notification.getUserId())));
        return mapper.toResult(teamMate);
    }

    @Transactional
    public void inviteReject(TeamMateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        TeamMate teamMate = applyToTeamMate(notification.getLongAttribute("teamMateId"),
                TeamMate::toRejectStatus);

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_REJECT,
                getInviteRejectNotificationDto(teamMate, notification.getUserId())));
    }

    @Transactional
    public void updateHookyTeamMate() {
        List<TeamMate> hookyTMs = teamMateRepository.updateYesterdayHookyTMs();
        List<TeamMate> eliminators = teamMateRepository.eliminateOveredTMs(hookyTMs);
        List<ExpulsionGoalNotificationDto> notificationDtos =
                eliminators.stream().map(mapper::toNotificationDto).toList();

        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(EXPULSION_GOAL, notificationDtos));
        cacheHandler.deleteTeamMateCaches(eliminators);
    }

    private TeamMate applyToTeamMate(long teamMateId, Consumer<TeamMate> consumer) {
        TeamMate teamMate = findTeamMateWithGoal(teamMateId);
        consumer.accept(teamMate);
        return teamMate;
    }

    private TeamMate findOrCreateInvitee(long goalId, String inviteeNickname) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        User invitee = userRepository.findByNickname(inviteeNickname)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        return teamMateRepository.findTeamMateWithGoal(goal.getId(), invitee.getId())
                .orElseGet(() -> teamMateRepository.save(goal.join(invitee)));
    }

    private Notification findAndReadNotification(long notificationId, long inviteeUserId) {
        NotificationReceiver receiver = notificationRepository.findNotificationReceiver(notificationId, inviteeUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        receiver.read();
        return receiver.getNotification();
    }

    private TeamMateInviteNotificationDto getInviteGoalNotificationDto(TeamMateInviteCommand command, Long inviteeUserId) {
        TeamMate inviteeTeamMate = findTeamMateWithGoal(command.goalId(), inviteeUserId);
        return mapper.toNotificationDto(command.inviterUserId(), findNickname(command.inviterUserId()), inviteeTeamMate);
    }

    private NotificationCreateDto getInviteAcceptNotificationDto(TeamMate invitee, long inviterUserId) {
        return mapper.toAcceptNotificationDto(invitee, findNickname(invitee.getUserId()), inviterUserId);
    }

    private InviteRejectNotificationDto getInviteRejectNotificationDto(TeamMate invitee, long inviterUserId) {
        return mapper.toRejectNotificationDto(invitee, findNickname(invitee.getUserId()), inviterUserId);
    }

    private String findNickname(long userId) {
        return userRepository.findNicknameById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    private TeamMate findTeamMateWithGoal(long goalId, long userId) {
        return teamMateRepository.findTeamMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND, userId));
    }

    private TeamMate findTeamMateWithGoal(long teamMateId) {
        return teamMateRepository.findTeamMateWithGoal(teamMateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND, teamMateId));
    }
}
