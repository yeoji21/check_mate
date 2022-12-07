package checkmate.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class NotificationTest {

    @Test @DisplayName("Notification 생성")
    void create() throws Exception{
        //given when
        Notification notification = Notification.builder()
                .userId(1L)
                .type(NotificationType.INVITE_GOAL)
                .title("title")
                .content("content")
                .receivers(List.of(new NotificationReceiver(2L), new NotificationReceiver(3L)))
                .build();
        notification.addAttribute("1", 1);
        notification.addAttribute("key", "123");

        //then
        assertThat(notification).isNotNull();
        assertThat(notification.getReceivers().size()).isEqualTo(2);
        assertThat(notification.getStringAttribute("1")).isEqualTo("1");
        assertThat(notification.getStringAttribute("key")).isEqualTo("123");
    }

}