package checkmate.notification.application;

import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.request.NotificationDetailsCriteria;
import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.application.dto.response.NotificationDetailResult;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.notification.presentation.dto.NotificationAttributeInfoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationQueryService {
    private final NotificationRepository notificationRepository;
    private final NotificationQueryDao notificationQueryDao;
    private final NotificationQueryMapper mapper;

    @Transactional
    public NotificationAttributeInfo findNotificationInfo(long notificationId, long userId) {
        NotificationReceiver receiver = notificationRepository.findReceiver(notificationId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        read(receiver);
        return mapper.toInfo(receiver.getNotification());
    }

    @Transactional
    public NotificationAttributeInfoResult findGoalCompleteNotifications(long userId) {
        List<NotificationAttributeInfo> notifications = notificationRepository.findUnCheckedReceivers(userId, NotificationType.COMPLETE_GOAL)
                .stream()
                .map(receiver -> {
                    receiver.read();
                    return mapper.toInfo(receiver.getNotification());
                })
                .toList();
        return new NotificationAttributeInfoResult(notifications);
    }

    @Transactional(readOnly = true)
    public NotificationDetailResult findNotificationDetails(NotificationDetailsCriteria criteria) {
        return notificationQueryDao.findNotificationDetailResult(
                criteria.userId(),
                criteria.cursorId(),
                PageRequest.of(0, criteria.size())
        );
    }

    private void read(NotificationReceiver receiver) {
        if (receiver.getNotification().getType() != NotificationType.INVITE_GOAL)
            receiver.read();
    }
}
