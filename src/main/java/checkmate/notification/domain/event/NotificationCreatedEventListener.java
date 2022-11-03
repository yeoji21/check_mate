package checkmate.notification.domain.event;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.push.PushNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationCreatedEventListener {
    private final NotificationRepository repository;
    private final NotificationGenerator generator;
    private final PushNotificationSender pushNotificationSender;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushNotification(PushNotificationCreatedEvent event){
        Notification notification = generator.generate(event.getNotificationType(), event.getCreateCommand());
        repository.save(notification);
        List<String> tokens = repository.findReceiversFcmToken(notification.getId());
        pushNotificationSender.send(notification, tokens);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void staticNotification(StaticNotificationCreatedEvent event) {
        repository.saveAll(
                event.getCreateCommand()
                        .stream()
                        .map(o -> generator.generate(event.getNotificationType(), o))
                        .collect(Collectors.toList())
        );
    }
}
