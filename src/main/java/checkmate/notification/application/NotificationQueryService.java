package checkmate.notification.application;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.NotFoundException;
import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.request.NotificationDetailsCriteria;
import checkmate.notification.application.dto.response.NotificationDetailsResult;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.NotificationQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class NotificationQueryService {
    private final NotificationRepository notificationRepository;
    private final NotificationQueryDao notificationQueryDao;
    private final NotificationQueryMapper mapper;

    @Transactional
    public NotificationInfo findNotificationInfo(long notificationId, long userId) {
        NotificationReceiver receiver = notificationRepository.findNotificationReceiver(notificationId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        read(receiver);
        return mapper.toInfo(receiver.getNotification());
    }

    @Transactional(readOnly = true)
    public NotificationDetailsResult findNotificationDetails(NotificationDetailsCriteria criteria) {
        return notificationQueryDao.findNotificationDetailResult(
                criteria.getUserId(),
                criteria.getCursorId(),
                PageRequest.of(0, criteria.getSize())
        );
    }

    @Transactional
    public List<NotificationInfo> findGoalCompleteNotifications(long userId) {
        return notificationRepository.findGoalCompleteNotificationReceivers(userId)
                .stream()
                .map(receiver -> {
                    receiver.read();
                    return mapper.toInfo(receiver.getNotification());
                })
                .collect(Collectors.toList());
    }

    private void read(NotificationReceiver receiver) {
        if(receiver.getNotification().getType() != NotificationType.INVITE_GOAL)
            receiver.read();
    }
}
