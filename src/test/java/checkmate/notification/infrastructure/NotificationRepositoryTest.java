package checkmate.notification.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.code.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest extends RepositoryTest {
    @Test
    void findByNotificationId() throws Exception{
        //given
        User sender = TestEntityFactory.user(null, "tester");
        em.persist(sender);

        Notification notification = Notification.builder()
                .userId(sender.getId())
                .type(NotificationType.INVITE_GOAL)
                .title("notification title")
                .content("notification body")
                .receivers(List.of(new NotificationReceiver(2L), new NotificationReceiver(3L)))
                .build();
        em.persist(notification);

        //when
        Notification findNotification = notificationRepository.findById(notification.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notification.getId()));

        //then
        assertThat(findNotification.getId()).isEqualTo(notification.getId());
        assertThat(findNotification.getTitle()).contains("title");
        assertThat(findNotification.getReceivers().size()).isEqualTo(2);
    }

    @Test
    void findReceiversFcmToken() throws Exception{
        //given
        User sender = TestEntityFactory.user(null, "tester");
        em.persist(sender);

        User receiver1 = TestEntityFactory.user(null, "receiver1");
        em.persist(receiver1);

        User receiver2 = TestEntityFactory.user(null, "receiver2");
        em.persist(receiver2);

        Notification notification = Notification.builder()
                .userId(sender.getId())
                .type(NotificationType.INVITE_GOAL)
                .title("notification title")
                .content("notification body")
                .receivers(List.of(new NotificationReceiver(receiver1.getId()), new NotificationReceiver(receiver2.getId())))
                .build();
        em.persist(notification);

        //when
        List<String> fcmTokens = notificationRepository.findReceiversFcmToken(notification.getId());

        //then
        assertThat(fcmTokens.size()).isEqualTo(notification.getReceivers().size());
        for (String fcmToken : fcmTokens) assertThat(fcmToken.isBlank()).isFalse();
    }
}