package checkmate.notification.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirebaseNotificationSenderTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;
    @InjectMocks
    private FirebaseNotificationSender firebaseNotificationSender;

    @Test
    @DisplayName("단일 푸쉬 알림 전송")
    void sendAsync() throws Exception {
        //given
        Notification notification = TestEntityFactory.notification(1L, 1L,
            NotificationType.INVITE_SEND);
        List<String> tokens = List.of("FCM Token");

        //when
        firebaseNotificationSender.send(notification, tokens);

        //then
        verify(firebaseMessaging).sendAsync(any(Message.class));
    }

    @Test
    @DisplayName("다중 푸쉬 알림 전송")
    void sendMulticastAsync() throws Exception {
        //given
        Notification notification = TestEntityFactory.notification(1L, 1L,
            NotificationType.INVITE_SEND);
        List<String> tokens = List.of("FCM Token1", "FCM Token2", "FCM Token3");

        //when
        firebaseNotificationSender.send(notification, tokens);

        //then
        verify(firebaseMessaging).sendMulticastAsync(any(MulticastMessage.class));
    }
}