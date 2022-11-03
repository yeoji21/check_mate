package checkmate.notification.domain.push;

import checkmate.TestEntityFactory;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.FcmSingleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PushNotificationSenderTest {
    @Mock private PushNotificationFactory pushNotificationFactory;
    private PushNotificationSendStrategy strategy;
    private boolean called;
    private PushNotificationSender notificationSender;

    @BeforeEach
    void setUp() {
        called = false;

        strategy = new PushNotificationSendStrategy() {
            @Override
            public void send(PushNotification pushNotification) {
                called = true;
            }

            @Override
            public Class<? extends PushNotification> getMessageType() {
                return FcmSingleMessage.class;
            }
        };

        notificationSender = new PushNotificationSender(List.of(strategy), pushNotificationFactory);
    }

    @Test
    void 전송_테스트() throws Exception {
        //given
        Notification notification = TestEntityFactory.notification(1L, 1L, null);
        ReflectionTestUtils.setField(notification, "notificationType", NotificationType.POST_UPLOAD);
        FcmSingleMessage message = FcmSingleMessage.getMessage(notification, "token");

        given(pushNotificationFactory.create(any(Notification.class), any(List.class))).willReturn(message);

        //when
        notificationSender.send(notification, List.of("token"));

        //then
        assertThat(called).isTrue();
    }
}