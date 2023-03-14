package checkmate.notification.domain;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void saveAll(Iterable<Notification> notifications);

    void save(Notification notification);

    Optional<NotificationReceiver> findNotificationReceiver(long notificationId, long receiverUserId);

    List<String> findReceiversFcmToken(Long notificationId);

    List<NotificationReceiver> findGoalCompleteNotificationReceivers(long userId);
}
