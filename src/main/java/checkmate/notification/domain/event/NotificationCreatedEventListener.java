package checkmate.notification.domain.event;

import checkmate.notification.application.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class NotificationCreatedEventListener {

    private final NotificationCommandService notificationCommandService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushNotification(PushNotificationCreatedEvent event) {
        notificationCommandService.savePushNotification(event.getNotificationType(),
            event.getCreateDto());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notPushNotification(NotPushNotificationCreatedEvent event) {
        notificationCommandService.saveNotPushNotifications(event.getNotificationType(),
            event.getCreateDto());
    }
}
