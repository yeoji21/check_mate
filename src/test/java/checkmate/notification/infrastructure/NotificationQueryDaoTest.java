package checkmate.notification.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.notification.application.dto.response.NotificationDetailsResult;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class NotificationQueryDaoTest extends RepositoryTest {

    @Test
    void findNotificationDetailResult() throws Exception {
        //given
        User sender = TestEntityFactory.user(null, "sender");
        em.persist(sender);
        User receiver1 = TestEntityFactory.user(1L, "receiver1");
        User receiver2 = TestEntityFactory.user(2L, "receiver2");

        Notification notification1 = Notification.builder()
                .title("notification title2")
                .body("notification body2")
                .userId(sender.getId())
                .build();
        notification1.setNotificationType(NotificationType.INVITE_GOAL);
        notification1.setUpReceivers(List.of(new NotificationReceiver(receiver1.getId()), new NotificationReceiver(receiver2.getId())));
        em.persist(notification1);

        Notification notification2 = Notification.builder()
                .title("notification title")
                .body("notification body")
                .userId(sender.getId())
                .build();
        notification2.setNotificationType(NotificationType.POST_UPLOAD);
        notification2.setUpReceivers(List.of(new NotificationReceiver(receiver1.getId()), new NotificationReceiver(receiver2.getId())));
        em.persist(notification2);

        em.flush();
        em.clear();

        //when
        NotificationDetailsResult result =
                notificationQueryDao.findNotificationDetailResult(receiver1.getId(), null, PageRequest.of(0, 10));

        result.getNotificationDetails()
                .forEach(n -> System.out.println(n.getNotificationId()));

        //then
        assertThat(result.getNotificationDetails().size()).isEqualTo(2);
        assertThat(result.getNotificationDetails().get(0).getNotificationId()).isGreaterThan(result.getNotificationDetails().get(1).getNotificationId());
        assertThat(result.getNotificationDetails().get(0).getType()).isEqualTo(NotificationType.POST_UPLOAD.toString());
        assertThat(result.getNotificationDetails().get(1).getBody()).contains("body");
    }

    @Test
    void findGoalCompleteByUserId() throws Exception{
        //given
        User sender = TestEntityFactory.user(null, "sender");
        em.persist(sender);
        User receiver = TestEntityFactory.user(null, "receiver");
        em.persist(receiver);

        Notification notification = Notification.builder()
                .title("notification title")
                .body("notification body")
                .userId(sender.getId())
                .build();
        notification.setNotificationType(NotificationType.COMPLETE_GOAL);
        notification.setUpReceivers(List.of(new NotificationReceiver(receiver.getId())));
        em.persist(notification);

        em.flush();
        em.clear();

        //when
        List<NotificationReceiver> receivers = notificationRepository.findGoalCompleteNotificationReceivers(receiver.getId());

        //then
        assertThat(receivers.size()).isEqualTo(1);
        assertThat(receivers.get(0).getNotification().getNotificationType()).isEqualTo(NotificationType.COMPLETE_GOAL);
        assertThat(receivers.get(0).getUserId()).isEqualTo(receiver.getId());
        assertThat(receivers.get(0).getNotification().getUserId()).isEqualTo(sender.getId());
    }
}