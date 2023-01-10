package checkmate.notification.infrastructure;

import checkmate.notification.domain.Notification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO: 2023/01/10 TEST
@Component
public class PushNotificationSender {
    public void send(Notification notification, List<String> tokens) {
        com.google.firebase.messaging.Notification FCMNotification =
                new com.google.firebase.messaging.Notification(notification.getTitle(), notification.getContent());
        if(tokens.size() == 1){
            Message message = Message.builder()
                    .setNotification(FCMNotification)
                    .setToken(tokens.get(0))
                    .build();
            FirebaseMessaging.getInstance().sendAsync(message);
        }
        else{
            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .setNotification(FCMNotification)
                    .addAllTokens(tokens)
                    .build();
            FirebaseMessaging.getInstance().sendMulticastAsync(multicastMessage);
        }
    }
}
