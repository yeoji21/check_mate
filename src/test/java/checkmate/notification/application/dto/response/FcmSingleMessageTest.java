package checkmate.notification.application.dto.response;

import checkmate.TestEntityFactory;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.FcmMultipleMessage;
import checkmate.notification.infrastructure.FcmSingleMessage;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FcmSingleMessageTest {
    @Test
    void 단건_FcmMessage_생성_테스트() throws Exception{
        //given
        User user = TestEntityFactory.user(1L, "tester");
        user.updateFcmToken("token");

        Notification notification = Notification.builder()
                .userId(1L)
                .title("title")
                .content("body")
                .type(NotificationType.INVITE_GOAL)
                .receivers(List.of(new NotificationReceiver(user.getId())))
                .build();
        ReflectionTestUtils.setField(notification, "id", 1L);

        //when
        FcmSingleMessage fcmSingleMessage = FcmSingleMessage.getMessage(notification, user.getFcmToken());
        FcmSingleMessage.Message message = fcmSingleMessage.getMessage();

        //then
        assertThat(message.getToken()).isNotNull();
        assertThat(message.getData().getType()).isEqualTo(NotificationType.INVITE_GOAL.name());
        assertThat(message.getData().getNotificationId()).isEqualTo("1");
        assertThat(message.getData().getTitle()).isEqualTo(notification.getTitle());
        assertThat(message.getData().getBody()).isEqualTo(notification.getContent());
    }

    @Test
    void 다중_FcmMessage_생성_테스트() throws Exception{
        //given
        Notification notification = getTestInstance();
        List<String> tokens = List.of("id1", "id2", "id3");

        //when
        FcmMultipleMessage multipleMessage = FcmMultipleMessage.getMessages(notification, tokens);

        //then
        assertThat(multipleMessage.getData().getType()).isEqualTo(NotificationType.INVITE_GOAL.name());
        assertThat(multipleMessage.getData().getTitle()).isEqualTo(notification.getTitle());
        assertThat(multipleMessage.getData().getBody()).isEqualTo(notification.getContent());
        assertThat(multipleMessage.getData().getNotificationId()).isEqualTo("1");
        assertThat(multipleMessage.getRegistration_ids().size()).isEqualTo(3);
        assertThat(multipleMessage.getRegistration_ids()).contains("id1", "id2", "id3");
    }

    private Notification getTestInstance() {
        User user1 = TestEntityFactory.user(1L, "user1");
        user1.updateFcmToken("fcmToken");

        User user2 = TestEntityFactory.user(2L, "user2");
        user2.updateFcmToken("fcmToken");

        User user3 = TestEntityFactory.user(3L, "user3");
        user3.updateFcmToken("fcmToken");

        Notification notification = Notification.builder()
                .userId(1L)
                .type(NotificationType.INVITE_GOAL)
                .title("title")
                .content("body")
                .receivers(List.of(new NotificationReceiver(user2.getId()), new NotificationReceiver(user3.getId())))
                .build();
        ReflectionTestUtils.setField(notification, "id", 1L);
        return notification;
    }
}