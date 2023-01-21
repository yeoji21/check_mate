package checkmate.goal.application;

import checkmate.common.cache.CacheTemplate;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.InviteReplyNotificationDto;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CacheTemplate cacheTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final TeamMateCommandMapper mapper;

    @Transactional
    public void inviteTeamMate(TeamMateInviteCommand command) {
        TeamMate teamMate = invite(command.goalId(), command.inviteeNickname());
        teamMate.toWaitingStatus();
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_GOAL,
                getInviteGoalNotificationDto(command, teamMate.getUserId())));
    }

    @Transactional
    public TeamMateAcceptResult inviteAccept(TeamMateInviteReplyCommand command) {
        return mapper.toResult(
                inviteReply(command,
                (teamMate) -> teamMate.initiateGoal(goalRepository.countOngoingGoals(teamMate.getUserId())),
                true)
        );
    }

    @Transactional
    public void inviteReject(TeamMateInviteReplyCommand command) {
        inviteReply(command, TeamMate::toRejectStatus, false);
    }

    @Transactional
    public void updateHookyTeamMate() {
        List<TeamMate> hookyTMs = teamMateRepository.updateYesterdayHookyTMs();
        List<TeamMate> eliminators = teamMateRepository.eliminateOveredTMs(hookyTMs);

        eventPublisher.publishEvent(
                new NotPushNotificationCreatedEvent(EXPULSION_GOAL,
                mapper.toKickOutNotificationDtos(eliminators))
        );
        cacheTemplate.deleteTMCacheData(eliminators);
    }

    private TeamMate inviteReply(TeamMateInviteReplyCommand command, Consumer<TeamMate> consumer, boolean accept) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        TeamMate teamMate = findTeamMateWithGoal(notification.getLongAttribute("teamMateId"));
        consumer.accept(teamMate);
        // TODO: 2023/01/19 boolean flag로 분기하는 로직 개선 고려
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_GOAL_REPLY,
                getInviteReplyNotificationDto(teamMate, notification.getUserId(), accept)));
        return teamMate;
    }

    private TeamMate invite(long goalId, String inviteeNickname) {
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
        User sender = findUser(command.inviterUserId());
        return mapper.toInviteGoalNotificationDto(sender, inviteeTeamMate);
    }

    private InviteReplyNotificationDto getInviteReplyNotificationDto(TeamMate invitee, long inviterUserId, boolean accept) {
        String inviteeNickname = findUser(invitee.getUserId()).getNickname();
        return mapper.toInviteReplyNotificationDto(invitee, inviteeNickname, inviterUserId, accept);
    }

    private TeamMate findTeamMateWithGoal(long goalId, long userId) {
        return teamMateRepository.findTeamMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND, userId));
    }

    private TeamMate findTeamMateWithGoal(long teamMateId) {
        return teamMateRepository.findTeamMateWithGoal(teamMateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND, teamMateId));
    }

    private User findUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }
}
