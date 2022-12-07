package checkmate.goal.application;

import checkmate.common.cache.CacheTemplate;
import checkmate.exception.code.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.response.TeamMateInviteReplyResult;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.goal.domain.service.TeamMateInviteService;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.InviteGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteReplyNotificationDto;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static checkmate.exception.code.ErrorCode.USER_NOT_FOUND;
import static checkmate.notification.domain.NotificationType.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamMateCommandService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final TeamMateRepository teamMateRepository;
    private final NotificationRepository notificationRepository;
    private final TeamMateInviteService inviteService;
    private final CacheTemplate cacheTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final TeamMateCommandMapper mapper;

    @Transactional
    public void initiatingGoalCreator(long goalId, long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND, userId));
        TeamMate teamMate = goal.join(creator);
        teamMate.initiateGoal(goalRepository.countOngoingGoals(userId));
        teamMateRepository.save(teamMate);
    }

    // TODO: 2022/12/08 inviteService 존재 여부
    @Transactional
    public void inviteTeamMate(TeamMateInviteCommand command) {
        Goal goal = findGoal(command.getGoalId());
        User invitee = findUser(command.getInviteeNickname());
        Optional<TeamMate> teamMate = teamMateRepository.findTeamMateWithGoal(goal.getId(), invitee.getId());
        inviteService.invite(goal, teamMate, invitee);
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_GOAL,
                getInviteGoalNotificationDto(command, invitee.getId())));
    }

    // TODO: 2022/07/20 초대 수락/거절 두 가지 일을 처리하는 메소드
    @Transactional
    public TeamMateInviteReplyResult applyInviteReply(TeamMateInviteReplyCommand command) {
        TeamMate invitee = teamMateRepository.findTeamMateWithGoal(command.getTeamMateId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND, command.getTeamMateId()));
        Notification inviteNotification = findAndReadNotification(command.getNotificationId(), invitee.getUserId());
        updateForInviteReply(invitee, command.isAccept());

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_GOAL_REPLY,
                getInviteReplyNotificationDto(invitee, inviteNotification.getUserId(), command.isAccept())));
        return mapper.toInviteReplyResult(invitee.getGoal().getId());
    }

    @Transactional
    public void updateHookyTeamMate() {
        List<TeamMate> hookyTMs = teamMateRepository.updateYesterdayHookyTMs();
        List<TeamMate> eliminators = teamMateRepository.eliminateOveredTMs(hookyTMs);

        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(EXPULSION_GOAL,
                mapper.toKickOutNotificationDtos(eliminators)));
        cacheTemplate.deleteTMCacheData(eliminators);
    }

    private Notification findAndReadNotification(long notificationId, long inviteeUserId) {
        NotificationReceiver receiver = notificationRepository.findNotificationReceiver(notificationId, inviteeUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        receiver.read();
        return receiver.getNotification();
    }

    private InviteGoalNotificationDto getInviteGoalNotificationDto(TeamMateInviteCommand command, Long inviteeUserId) {
        TeamMate inviteeTeamMate = findInviteeTeamMate(command.getGoalId(), inviteeUserId);
        User sender = findUser(command.getInviterUserId());
        return mapper.toInviteGoalNotificationDto(sender, inviteeTeamMate);
    }

    private InviteReplyNotificationDto getInviteReplyNotificationDto(TeamMate invitee, long inviterUserId, boolean accept) {
        String inviteeNickname = findUser(invitee.getUserId()).getNickname();
        return mapper.toInviteReplyNotificationDto(invitee, inviteeNickname, inviterUserId, accept);
    }

    private void updateForInviteReply(TeamMate invitee, boolean accept) {
        if(accept) {
            int ongoingGoalCount = goalRepository.countOngoingGoals(invitee.getUserId());
            invitee.initiateGoal(ongoingGoalCount);
        }
        else
            invitee.applyInviteReject();
    }

    private TeamMate findInviteeTeamMate(long goalId, long userId) {
        return teamMateRepository.findTeamMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND));
    }

    private Goal findGoal(long goalId) {
        return goalRepository.findById(goalId).orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    private User findUser(String nickname) {
        return userRepository.findByNickname(nickname).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private User findUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }
}
