package checkmate.notification.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.NotificationNotFoundException;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest extends RepositoryTest {
    @Test
    void findByNotificationId() throws Exception{
        //given
        User sender = TestEntityFactory.user(null, "tester");
        em.persist(sender);

        Notification notification = Notification.builder()
                .title("notification title")
                .body("notification body")
                .userId(sender.getId())
                .build();
        notification.setNotificationType(NotificationType.INVITE_GOAL);
        notification.setUpReceivers(List.of(new NotificationReceiver(2L), new NotificationReceiver(3L)));
        em.persist(notification);

        //when
        Optional<Notification> optionalNotification = notificationRepository.findById(notification.getId());
        Notification findNotification = optionalNotification.orElseThrow(NotificationNotFoundException::new);

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
                .title("notification title")
                .body("notification body")
                .userId(sender.getId())
                .build();
        notification.setNotificationType(NotificationType.INVITE_GOAL);
        notification.setUpReceivers(List.of(new NotificationReceiver(receiver1.getId()),
                new NotificationReceiver(receiver2.getId())));
        em.persist(notification);

        //when
        List<String> fcmTokens = notificationRepository.findReceiversFcmToken(notification.getId());

        //then
        assertThat(fcmTokens.size()).isEqualTo(notification.getReceivers().size());
        for (String fcmToken : fcmTokens) assertThat(fcmToken.isBlank()).isFalse();
    }
}