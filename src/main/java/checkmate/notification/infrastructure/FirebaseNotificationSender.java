package checkmate.notification.infrastructure;

import checkmate.notification.domain.NotificationSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("production")
@AllArgsConstructor
@Component
public class FirebaseNotificationSender implements NotificationSender {
    private final FirebaseMessaging firebaseMessaging;

    public void send(checkmate.notification.domain.Notification notification, List<String> tokens) {
        com.google.firebase.messaging.Notification fcmNotification =
                new com.google.firebase.messaging.Notification(notification.getTitle(), notification.getContent());
        if (tokens.size() == 1) {
            Message message = Message.builder()
                    .setNotification(fcmNotification)
                    .setToken(tokens.get(0))
                    .build();
            firebaseMessaging.sendAsync(message);
        } else {
            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .setNotification(fcmNotification)
                    .addAllTokens(tokens)
                    .build();
            firebaseMessaging.sendMulticastAsync(multicastMessage);
        }
    }
}