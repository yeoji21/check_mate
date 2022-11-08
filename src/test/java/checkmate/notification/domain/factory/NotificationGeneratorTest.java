package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class NotificationGeneratorTest {
    @Autowired
    NotificationGenerator notificationGenerator;

    @Test
    void generate() throws Exception{
        //given
        CompleteGoalNotificationDto dto = new CompleteGoalNotificationDto(1L, 1L, "test");

        //when
        Notification notification = notificationGenerator.generate(NotificationType.COMPLETE_GOAL, dto);

        //then
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.COMPLETE_GOAL);
    }
}






