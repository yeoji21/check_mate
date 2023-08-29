package checkmate.mate.application;

import static checkmate.notification.domain.NotificationType.INVITE_ACCEPT;
import static checkmate.notification.domain.NotificationType.INVITE_REJECT;
import static checkmate.notification.domain.NotificationType.INVITE_SEND;

import checkmate.common.cache.CacheKeyUtil;
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
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.InviteAcceptNotificationDto;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.InviteSendNotificationDto;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
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
    private final ApplicationEventPublisher eventPublisher;
    private final MateCommandMapper mapper;

    @Transactional
    public void sendInvite(MateInviteCommand command) {
        Mate invitee = findOrCreateMateToInvite(command.goalId(), command.inviteeNickname());
        invitee.receiveInvite();
        publishInviteSendEvent(command, invitee);
    }

    @Caching(evict = {
        @CacheEvict(
            value = CacheKeyUtil.ONGOING_GOALS,
            key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}"),
        @CacheEvict(value =
            CacheKeyUtil.TODAY_GOALS,
            key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}")
    })
    @Transactional
    public MateAcceptResult acceptInvite(MateInviteReplyCommand command) {
        // TODO: 2023/08/29 초대 수락 외에도 알림 읽음 처리를 하고 있음
        // 읽음 처리 어떻게 할지? attribute 꺼낼 때?
        Notification notification = findAndReadNotification(command.notificationId(),
            command.userId());
        // TODO: 2023/08/29 mate 두 번 조회 중
        Mate mate = findMate(notification.getLongAttribute(NotificationAttributeKey.MATE_ID));
        initiateToGoal(mate);
        publishInviteAcceptEvent(notification, mate);
        return mapper.toResult(mate);
    }

    @Transactional
    public void rejectInvite(MateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(),
            command.userId());
        Mate mate = findMate(notification.getLongAttribute(NotificationAttributeKey.MATE_ID));
        mate.rejectInvite();
        publishInviteRejectEvent(notification, mate);
    }

    private void initiateToGoal(Mate mate) {
        mateRepository.findUninitiateMate(mate.getId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mate.getId()))
            .initiate();
    }

    // TODO: 2023/08/28 Service layer 내 분기 로직
    // 로직을 어떻게 옮길지
    private Mate findOrCreateMateToInvite(long goalId, String inviteeNickname) {
        User invitee = userRepository.findByNickname(inviteeNickname)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        return mateRepository.findWithGoal(goalId, invitee.getId())
            .orElseGet(() -> createAndSaveMate(goalId, invitee));
    }

    // TODO: 2023/08/28 테스트되고 있지 않은 영역
    private Mate createAndSaveMate(long goalId, User invitee) {
        Goal goal = goalRepository.find(goalId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        return mateRepository.save(goal.createMate(invitee));
    }

    private Notification findAndReadNotification(long notificationId, long userId) {
        NotificationReceiver receiver = notificationRepository.findReceiver(notificationId, userId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        receiver.read();
        return receiver.getNotification();
    }

    private void publishInviteRejectEvent(Notification notification, Mate mate) {
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_REJECT,
            createInviteRejectNotificationDto(mate, notification.getUserId())));
    }

    private void publishInviteAcceptEvent(Notification notification, Mate mate) {
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_ACCEPT,
            createInviteAcceptNotificationDto(mate, notification.getUserId())));
    }

    private void publishInviteSendEvent(MateInviteCommand command, Mate invitee) {
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_SEND,
            createInviteSendNotificationDto(invitee, command)));
    }

    private InviteSendNotificationDto createInviteSendNotificationDto(Mate invitee,
        MateInviteCommand command) {
        return mapper.toNotificationDto(command.inviterUserId(),
            findNickname(command.inviterUserId()), invitee);
    }

    private InviteAcceptNotificationDto createInviteAcceptNotificationDto(Mate invitee,
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

    private Mate findMate(long mateId) {
        return mateRepository.findById(mateId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mateId));
    }
}
