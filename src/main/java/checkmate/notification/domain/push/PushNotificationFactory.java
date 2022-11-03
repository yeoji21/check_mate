package checkmate.notification.domain.push;

import checkmate.notification.domain.Notification;
import checkmate.notification.infrastructure.FcmMultipleMessage;
import checkmate.notification.infrastructure.FcmSingleMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PushNotificationFactory {
    public PushNotification create(Notification notification, List<String> fcmTokens) {
        return fcmTokens.size() == 1 ?
                FcmSingleMessage.getMessage(notification, fcmTokens.get(0)) :
                FcmMultipleMessage.getMessages(notification, fcmTokens);
    }
}
