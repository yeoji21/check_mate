package checkmate.mate.application;

import static checkmate.notification.domain.NotificationType.EXPULSION_GOAL;
import static checkmate.notification.domain.NotificationType.INVITE_ACCEPT;
import static checkmate.notification.domain.NotificationType.INVITE_GOAL;
import static checkmate.notification.domain.NotificationType.INVITE_REJECT;

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
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.MateStartingService;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.MateInviteNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MateCommandService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final MateRepository mateRepository;
    private final NotificationRepository notificationRepository;
    private final MateStartingService mateStartingService;
    private final CacheHandler cacheHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final MateCommandMapper mapper;

    @Transactional
    public void inviteMate(MateInviteCommand command) {
        Mate invitee = findOrCreateMate(command.goalId(), command.inviteeNickname());
        invitee.receivedInvite();
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
        Notification notification = findAndReadNotification(command.notificationId(),
            command.userId());
        Mate mate = find(notification.getLongAttribute(NotificationAttributeKey.MATE_ID));
        mateStartingService.startToGoal(mate);

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_ACCEPT,
            createInviteAcceptNotificationDto(mate, notification.getUserId())));
        return mapper.toResult(mate);
    }

    @Transactional
    public void inviteReject(MateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(),
            command.userId());
        Mate mate = find(notification.getLongAttribute(NotificationAttributeKey.MATE_ID));
        mate.rejectInvite();

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_REJECT,
            createInviteRejectNotificationDto(mate, notification.getUserId())));
    }

    @Transactional
    public void updateUploadSkippedMates() {
        List<Mate> skippedMates = updateYesterdaySkippedMates();
        List<Mate> limitOveredMates = filterLimitOveredMates(skippedMates);
        mateRepository.updateLimitOveredMates(limitOveredMates);

        publishExpulsionNotifications(limitOveredMates);
        cacheHandler.deleteUserCaches(limitOveredMates.stream().map(Mate::getUserId).toList());
    }

    private List<Mate> updateYesterdaySkippedMates() {
        List<Mate> skippedMates = mateRepository.findYesterdaySkippedMates();
        mateRepository.increaseSkippedDayCount(skippedMates);
        List<Long> mateIds = skippedMates.stream().map(Mate::getId).toList();
        skippedMates = mateRepository.findAllWithGoal(mateIds);
        return skippedMates;
    }

    private void publishExpulsionNotifications(List<Mate> limitOveredMates) {
        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(EXPULSION_GOAL,
            limitOveredMates.stream().map(mapper::toNotificationDto).toList()));
    }

    private List<Mate> filterLimitOveredMates(List<Mate> hookyMates) {
        return hookyMates.stream()
            .filter(tm -> tm.getSkippedDays() >= tm.getGoal().getSkippedDayLimit())
            .toList();
    }

    private Mate findOrCreateMate(long goalId, String inviteeNickname) {
        User invitee = userRepository.findByNickname(inviteeNickname)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // TODO: 2023/05/01 로직
        return mateRepository.findWithGoal(goalId, invitee.getId())
            .orElseGet(() -> {
                Goal goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
                return mateRepository.save(goal.createMate(invitee));
            });
    }

    private Notification findAndReadNotification(long notificationId, long inviteeUserId) {
        NotificationReceiver receiver = notificationRepository.findReceiver(notificationId,
                inviteeUserId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        receiver.read();
        return receiver.getNotification();
    }

    private MateInviteNotificationDto createInviteGoalNotificationDto(Mate inviteeMate,
        MateInviteCommand command) {
        return mapper.toNotificationDto(command.inviterUserId(),
            findNickname(command.inviterUserId()), inviteeMate);
    }

    private NotificationCreateDto createInviteAcceptNotificationDto(Mate invitee,
        long inviterUserId) {
        return mapper.toAcceptNotificationDto(invitee, findNickname(invitee.getUserId()),
            inviterUserId);
    }

    private InviteRejectNotificationDto createInviteRejectNotificationDto(Mate invitee,
        long inviterUserId) {
        return mapper.toRejectNotificationDto(invitee, findNickname(invitee.getUserId()),
            inviterUserId);
    }

    private String findNickname(long userId) {
        return userRepository.findNicknameById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    private Mate find(long teamMateId) {
        return mateRepository.findById(teamMateId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, teamMateId));
    }
}
