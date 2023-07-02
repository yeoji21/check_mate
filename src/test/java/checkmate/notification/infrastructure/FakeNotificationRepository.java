package checkmate.notification.infrastructure;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeNotificationRepository implements NotificationRepository {

    private final AtomicLong notificationId = new AtomicLong(1);
    private final Map<Long, Notification> map = new HashMap<>();


    @Override
    public void saveAll(Iterable<Notification> notifications) {
        notifications.forEach(this::save);
    }

    @Override
    public void save(Notification notification) {
        ReflectionTestUtils.setField(notification, "id", notificationId.getAndIncrement());
        map.put(notification.getId(), notification);
    }

    @Override
    public Optional<NotificationReceiver> findReceiver(long notificationId, long receiverUserId) {
        return map.get(notificationId).getReceivers().stream()
            .filter(receiver -> receiver.getUserId() == receiverUserId)
            .findAny();
    }

    @Override
    public List<NotificationReceiver> findUncheckedReceivers(long receiverUserId,
        NotificationType notificationType) {
        return map.values().stream()
            .filter(notification -> notification.getType() == notificationType)
            .map(notification -> notification.getReceivers().stream().filter(
                    receiver -> !receiver.isChecked() && receiver.getUserId() == receiverUserId)
                .findAny().orElse(null)
            ).toList();
    }
}
