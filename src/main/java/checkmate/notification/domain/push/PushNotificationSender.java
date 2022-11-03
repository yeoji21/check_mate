package checkmate.notification.domain.push;

import checkmate.notification.domain.Notification;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PushNotificationSender {
    private final Map<Class<? extends PushNotification>, PushNotificationSendStrategy> map = new HashMap<>();
    private final PushNotificationFactory pushNotificationFactory;

    public PushNotificationSender(List<PushNotificationSendStrategy> strategies,
                                  PushNotificationFactory pushNotificationFactory) {
        strategies.forEach(strategy -> map.put(strategy.getMessageType(), strategy));
        this.pushNotificationFactory = pushNotificationFactory;
    }

    public void send(Notification notification, List<String> tokens) {
        PushNotification pushNotification = pushNotificationFactory.create(notification, tokens);
        map.get(pushNotification.getClass()).send(pushNotification);
    }
}
