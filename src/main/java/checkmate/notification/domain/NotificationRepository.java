package checkmate.notification.domain;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void saveAll(Iterable<Notification> notifications);

    void save(Notification notification);

    Optional<NotificationReceiver> findReceiver(long notificationId, long receiverUserId);

    List<NotificationReceiver> findUncheckedReceivers(long receiverUserId, NotificationType notificationType);
}
