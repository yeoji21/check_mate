package checkmate.notification.domain.event;

import checkmate.notification.application.NotificationPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class NotificationCreatedEventListener {
    private final NotificationPushService notificationPushService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushNotification(PushNotificationCreatedEvent event){
        notificationPushService.push(event.getNotificationType(), event.getCreateDto());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notPushNotification(NotPushNotificationCreatedEvent event) {
        notificationPushService.notPush(event.getNotificationType(), event.getCreateDto());
    }
}
