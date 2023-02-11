package checkmate;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NotiTest {
    @Autowired
    private NotificationGenerator generator;

    @Test
    void test() throws Exception {
        //given
        InviteRejectNotificationDto dto = new InviteRejectNotificationDto(1L, "nickname",
                1L, "title", 1L);

        //when
        Notification notification = generator.generate(NotificationType.INVITE_REJECT, dto);

        //then
        System.out.println(notification.getContent());
    }
}
