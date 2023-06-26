package checkmate.notification.infrastructure;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;
import static checkmate.notification.domain.NotificationType.INVITE_SEND;
import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.user.domain.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationRepositoryTest extends RepositoryTest {

    @Test
    @DisplayName("알림 수신자 조회")
    void findNotificationReceiver() throws Exception {
        //given
        User sender = createUser("sender");
        User receiver = createUser("receiver");
        Notification notification = createNotification(sender, INVITE_SEND,
            List.of(new NotificationReceiver(receiver.getId())));
        em.flush();
        em.clear();

        //when
        NotificationReceiver notificationReceiver = notificationRepository.findReceiver(
                notification.getId(), receiver.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(notificationReceiver.getNotification()).isEqualTo(notification);
        assertThat(notification.getReceivers()).contains(notificationReceiver);
        assertThat(notificationReceiver.getUserId()).isEqualTo(receiver.getId());
    }

    @Test
    @DisplayName("수신하지 않은 목표 완료 알림 조회")
    void findGoalCompleteNotificationReceivers() throws Exception {
        //given
        User sender = createUser("sender");
        User receiver = createUser("receiver");

        createNotification(sender, COMPLETE_GOAL,
            List.of(new NotificationReceiver(receiver.getId())));
        NotificationReceiver checkedReceiver = new NotificationReceiver(receiver.getId());
        ReflectionTestUtils.setField(checkedReceiver, "checked", true);
        createNotification(sender, COMPLETE_GOAL, List.of(checkedReceiver));

        em.flush();
        em.clear();

        //when
        List<NotificationReceiver> receivers = notificationRepository.findUncheckedReceivers(
            receiver.getId(), COMPLETE_GOAL);

        //then
        assertThat(receivers.size()).isEqualTo(1);
        assertThat(receivers.get(0).getNotification().getType()).isEqualTo(
            NotificationType.COMPLETE_GOAL);
        assertThat(receivers.get(0).getUserId()).isEqualTo(receiver.getId());
        assertThat(receivers.get(0).getNotification().getUserId()).isEqualTo(sender.getId());
    }

    private Notification createNotification(User sender,
        NotificationType type,
        List<NotificationReceiver> receiver) {
        Notification notification = Notification.builder()
            .userId(sender.getId())
            .type(type)
            .title("title")
            .content("body")
            .receivers(receiver)
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