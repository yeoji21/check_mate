package checkmate.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.TestEntityFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    @DisplayName("attribute 값 추가")
    void addAttribute() throws Exception {
        //given
        Notification notification = createNotification();

        //when
        notification.addAttribute(NotificationAttributeKey.GOAL_ID, 123);

        //then
        assertThat(notification.getLongAttribute(NotificationAttributeKey.GOAL_ID)).isEqualTo(123);
    }

    @Test
    @DisplayName("알림 수신자 추가")
    void setUpReceivers() throws Exception {
        //given
        List<NotificationReceiver> receivers = createNotificationReceivers();

        //when
        Notification notification = Notification.builder()
            .userId(1L)
            .type(NotificationType.INVITE_SEND)
            .title("title")
            .content("content")
            .receivers(receivers)
            .build();

        //then
        assertThat(notification.getReceivers()).hasSameElementsAs(receivers);
        assertThat(receivers).allMatch(receiver -> receiver.getNotification() == notification);
    }

    private List<NotificationReceiver> createNotificationReceivers() {
        List<NotificationReceiver> receivers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            receivers.add(new NotificationReceiver(i));
        }
        return receivers;
    }

    private Notification createNotification() {
        return TestEntityFactory.notification(1L, 1L, NotificationType.INVITE_SEND);
    }
}