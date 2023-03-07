package checkmate.mate.application;

import checkmate.common.cache.CacheHandler;
import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateInitiateManager;
import checkmate.mate.domain.MateRepository;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.MateInviteNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
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

import static checkmate.notification.domain.NotificationType.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class MateCommandService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final MateRepository mateRepository;
    private final NotificationRepository notificationRepository;
    private final MateInitiateManager mateInitiateManager;
    private final CacheHandler cacheHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final MateCommandMapper mapper;

    @Transactional
    public void inviteMate(MateInviteCommand command) {
        Mate invitee = findOrCreateMate(command.goalId(), command.inviteeNickname());
        invitee.toWaitingStatus();
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_GOAL,
                createInviteGoalNotificationDto(invitee, command)));
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
    public MateAcceptResult inviteAccept(MateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        Mate mate = findMateWithGoal(notification.getLongAttribute("mateId"));
        mateInitiateManager.initiate(mate);

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_ACCEPT,
                createInviteAcceptNotificationDto(mate, notification.getUserId())));
        return mapper.toResult(mate);
    }

    @Transactional
    public void inviteReject(MateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        Mate mate = findMateWithGoal(notification.getLongAttribute("mateId"));
        mate.toRejectStatus();

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_REJECT,
                createInviteRejectNotificationDto(mate, notification.getUserId())));
    }

    @Transactional
    public void updateUploadSkippedMates() {
        List<Mate> skippedMates = mateRepository.updateYesterdaySkippedMates();
        List<Mate> eliminatedMates = filterEliminateMates(skippedMates);
        mateRepository.eliminateOveredMates(eliminatedMates);

        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(EXPULSION_GOAL,
                eliminatedMates.stream().map(mapper::toNotificationDto).toList()));
        cacheHandler.deleteMateCaches(eliminatedMates);
    }

    private List<Mate> filterEliminateMates(List<Mate> hookyMates) {
        return hookyMates.stream()
                .filter(tm -> tm.getHookyDays() >= tm.getGoal().getSkippedDayLimit())
                .toList();
    }

    private Mate findOrCreateMate(long goalId, String inviteeNickname) {
        User invitee = userRepository.findByNickname(inviteeNickname)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return mateRepository.findMateWithGoal(goalId, invitee.getId())
                .orElseGet(() -> {
                    Goal goal = goalRepository.findById(goalId)
                            .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
                    return mateRepository.save(goal.join(invitee));
                });
    }

    private Notification findAndReadNotification(long notificationId, long inviteeUserId) {
        NotificationReceiver receiver = notificationRepository.findNotificationReceiver(notificationId, inviteeUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        receiver.read();
        return receiver.getNotification();
    }

    private MateInviteNotificationDto createInviteGoalNotificationDto(Mate inviteeMate, MateInviteCommand command) {
        return mapper.toNotificationDto(command.inviterUserId(), findNickname(command.inviterUserId()), inviteeMate);
    }

    private NotificationCreateDto createInviteAcceptNotificationDto(Mate invitee, long inviterUserId) {
        return mapper.toAcceptNotificationDto(invitee, findNickname(invitee.getUserId()), inviterUserId);
    }

    private InviteRejectNotificationDto createInviteRejectNotificationDto(Mate invitee, long inviterUserId) {
        return mapper.toRejectNotificationDto(invitee, findNickname(invitee.getUserId()), inviterUserId);
    }

    private String findNickname(long userId) {
        return userRepository.findNicknameById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    private Mate findMateWithGoal(long teamMateId) {
        return mateRepository.findMateWithGoal(teamMateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, teamMateId));
    }
}
