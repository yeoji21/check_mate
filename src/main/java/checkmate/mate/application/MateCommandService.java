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
import java.util.function.Consumer;

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
        Mate invitee = findOrCreateInvitee(command.goalId(), command.inviteeNickname());
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
    public MateAcceptResult inviteAccept(MateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        Mate mate = applyToMate(notification.getLongAttribute("mateId"),
                mateInitiateManager::initiate);

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_ACCEPT,
                getInviteAcceptNotificationDto(mate, notification.getUserId())));
        return mapper.toResult(mate);
    }

    @Transactional
    public void inviteReject(MateInviteReplyCommand command) {
        Notification notification = findAndReadNotification(command.notificationId(), command.userId());
        Mate mate = applyToMate(notification.getLongAttribute("mateId"),
                Mate::toRejectStatus);

        eventPublisher.publishEvent(new PushNotificationCreatedEvent(INVITE_REJECT,
                getInviteRejectNotificationDto(mate, notification.getUserId())));
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

    private Mate applyToMate(long teamMateId, Consumer<Mate> consumer) {
        Mate mate = findMateWithGoal(teamMateId);
        consumer.accept(mate);
        return mate;
    }

    private Mate findOrCreateInvitee(long goalId, String inviteeNickname) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        User invitee = userRepository.findByNickname(inviteeNickname)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        return mateRepository.findMateWithGoal(goal.getId(), invitee.getId())
                .orElseGet(() -> mateRepository.save(goal.join(invitee)));
    }

    private Notification findAndReadNotification(long notificationId, long inviteeUserId) {
        NotificationReceiver receiver = notificationRepository.findNotificationReceiver(notificationId, inviteeUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        receiver.read();
        return receiver.getNotification();
    }

    private MateInviteNotificationDto getInviteGoalNotificationDto(MateInviteCommand command, Long inviteeUserId) {
        Mate inviteeMate = findMateWithGoal(command.goalId(), inviteeUserId);
        return mapper.toNotificationDto(command.inviterUserId(), findNickname(command.inviterUserId()), inviteeMate);
    }

    private NotificationCreateDto getInviteAcceptNotificationDto(Mate invitee, long inviterUserId) {
        return mapper.toAcceptNotificationDto(invitee, findNickname(invitee.getUserId()), inviterUserId);
    }

    private InviteRejectNotificationDto getInviteRejectNotificationDto(Mate invitee, long inviterUserId) {
        return mapper.toRejectNotificationDto(invitee, findNickname(invitee.getUserId()), inviterUserId);
    }

    private String findNickname(long userId) {
        return userRepository.findNicknameById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    private Mate findMateWithGoal(long goalId, long userId) {
        return mateRepository.findMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, userId));
    }

    private Mate findMateWithGoal(long teamMateId) {
        return mateRepository.findMateWithGoal(teamMateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, teamMateId));
    }
}
