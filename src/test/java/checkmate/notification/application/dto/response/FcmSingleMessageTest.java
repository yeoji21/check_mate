package checkmate.notification.application.dto.response;

import checkmate.TestEntityFactory;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.infrastructure.FcmMultipleMessage;
import checkmate.notification.infrastructure.FcmSingleMessage;
import checkmate.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FcmSingleMessageTest {

    private Notification notification;
    @BeforeEach
    void setUp() {
        notification = getTestInstance(1L, NotificationType.INVITE_GOAL);
    }

    @Test
    void 단건_FcmMessage_생성_테스트() throws Exception{
        //given
        User user = TestEntityFactory.user(1L, "tester");
        user.updateFcmToken("token");
        Notification notification = TestEntityFactory.notification(1L, user.getId(), null);

        notification.setNotificationType(NotificationType.INVITE_GOAL);
        notification.setUpReceivers(List.of(new NotificationReceiver(user.getId())));

        //when
        FcmSingleMessage fcmSingleMessage = FcmSingleMessage.getMessage(notification, user.getFcmToken());
        FcmSingleMessage.Message message = fcmSingleMessage.getMessage();

        //then
        assertThat(message.getToken()).isNotNull();
        assertThat(message.getData().getType()).isEqualTo(NotificationType.INVITE_GOAL.name());
        assertThat(message.getData().getNotificationId()).isEqualTo("1");
        assertThat(message.getData().getTitle()).isEqualTo(notification.getTitle());
        assertThat(message.getData().getBody()).isEqualTo(notification.getBody());
    }

    @Test
    void 다중_FcmMessage_생성_테스트() throws Exception{
        //given
        List<String> ids = List.of("id1", "id2", "id3");

        //when
        FcmMultipleMessage multipleMessage = FcmMultipleMessage.getMessages(notification, ids);

        //then
        assertThat(multipleMessage.getData().getType()).isEqualTo(NotificationType.INVITE_GOAL.name());
        assertThat(multipleMessage.getData().getTitle()).isEqualTo(notification.getTitle());
        assertThat(multipleMessage.getData().getBody()).isEqualTo(notification.getBody());
        assertThat(multipleMessage.getData().getNotificationId()).isEqualTo("1");
        assertThat(multipleMessage.getRegistration_ids().size()).isEqualTo(3);
        assertThat(multipleMessage.getRegistration_ids()).contains("id1", "id2", "id3");
    }

    private Notification getTestInstance(Long id, NotificationType type) {
        User tester = TestEntityFactory.user(1L, "tester");
        tester.updateFcmToken("fcmToken");

        User tester2 = TestEntityFactory.user(2L, "tester");
        tester2.updateFcmToken("fcmToken");

        User tester3 = TestEntityFactory.user(3L, "tester");
        tester3.updateFcmToken("fcmToken");

        Notification notification = TestEntityFactory.notification(id, tester.getId(), type);
        notification.setUpReceivers(List.of(new NotificationReceiver(tester2.getId()), new NotificationReceiver(tester3.getId())));
        return notification;
    }
}