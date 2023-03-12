package checkmate.notification.domain;

import checkmate.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {
    @Test
    @DisplayName("attribute 값 추가")
    void addAttribute() throws Exception {
        //given
        Notification notification = createNotification();

        //when
        notification.addAttribute("key1", 123);
        notification.addAttribute("key2", "value");

        //then
        assertThat(notification.getLongAttribute("key1")).isEqualTo(123);
        assertThat(notification.getStringAttribute("key2")).isEqualTo("value");
    }

    @Test
    @DisplayName("알림 수신자 추가")
    void setUpReceivers() throws Exception {
        //given
        List<NotificationReceiver> receivers = createNotificationReceivers();

        //when
        Notification notification = Notification.builder()
                .userId(1L)
                .type(NotificationType.INVITE_GOAL)
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
        return TestEntityFactory.notification(1L, 1L, NotificationType.INVITE_GOAL);
    }
}