package checkmate.notification.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.notification.application.dto.response.NotificationDetailResult;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;
import static checkmate.notification.domain.NotificationType.POST_UPLOAD;
import static org.assertj.core.api.Assertions.assertThat;


class NotificationQueryDaoTest extends RepositoryTest {
    @Test
    @DisplayName("알림 상세 정보 조회")
    void findNotificationDetailResult() throws Exception {
        //given
        User sender = createUser("sender");
        User receiver1 = createUser("receiver1");
        User receiver2 = createUser("receiver2");

        createNotification(sender, POST_UPLOAD, createNotificationReceivers(receiver1, receiver2));
        createNotification(sender, POST_UPLOAD, createNotificationReceivers(receiver1, receiver2));

        em.flush();
        em.clear();

        //when
        NotificationDetailResult result = notificationQueryDao.findNotificationDetailResult(receiver1.getId(),
                null, PageRequest.of(0, 10));

        result.getNotifications()
                .forEach(n -> System.out.println(n.getNotificationId()));

        //then
        assertThat(result.getNotifications().size()).isEqualTo(2);
        assertThat(result.getNotifications().get(0).getNotificationId())
                .isGreaterThan(result.getNotifications().get(1).getNotificationId());
    }

    @Test
    @DisplayName("새로운 목표 완료 알림 조회")
    void findGoalCompleteNotificationReceivers() throws Exception {
        //given
        User sender = createUser("sender");
        User receiver = createUser("receiver");

        createNotification(sender, COMPLETE_GOAL, List.of(new NotificationReceiver(receiver.getId())));
        NotificationReceiver checkedReceiver = new NotificationReceiver(receiver.getId());
        ReflectionTestUtils.setField(checkedReceiver, "checked", true);
        createNotification(sender, COMPLETE_GOAL, List.of(checkedReceiver));

        em.flush();
        em.clear();

        //when
        List<NotificationReceiver> receivers = notificationRepository.findGoalCompleteNotificationReceivers(receiver.getId());

        //then
        assertThat(receivers.size()).isEqualTo(1);
        assertThat(receivers.get(0).getNotification().getType()).isEqualTo(NotificationType.COMPLETE_GOAL);
        assertThat(receivers.get(0).getUserId()).isEqualTo(receiver.getId());
        assertThat(receivers.get(0).getNotification().getUserId()).isEqualTo(sender.getId());
    }

    private List<NotificationReceiver> createNotificationReceivers(User user1, User user2) {
        return List.of(new NotificationReceiver(user1.getId()), new NotificationReceiver(user2.getId()));
    }

    private Notification createNotification(User sender, NotificationType type, List<NotificationReceiver> receivers) {
        Notification notification = Notification.builder()
                .userId(sender.getId())
                .type(type)
                .title("title")
                .content("content")
                .receivers(receivers)
                .build();
        em.persist(notification);
        return notification;
    }

    private User createUser(String nickname) {
        User sender = TestEntityFactory.user(null, nickname);
        em.persist(sender);
        return sender;
    }
}