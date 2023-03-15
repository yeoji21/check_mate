package checkmate.notification.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static checkmate.notification.domain.NotificationType.*;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest extends RepositoryTest {
    @Test @DisplayName("알림 수신자 조회")
    void findNotificationReceiver() throws Exception {
        //given
        User sender = createUser("sender");
        User receiver = createUser("receiver");
        Notification notification = createNotification(sender, INVITE_GOAL, List.of(new NotificationReceiver(receiver.getId())));
        em.flush();
        em.clear();

        //when
        NotificationReceiver notificationReceiver = notificationRepository.findNotificationReceiver(notification.getId(), receiver.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(notificationReceiver.getNotification()).isEqualTo(notification);
        assertThat(notification.getReceivers()).contains(notificationReceiver);
        assertThat(notificationReceiver.getUserId()).isEqualTo(receiver.getId());
    }

    @Test
    @DisplayName("알림 수신자 FCM 토큰 목록 조회")
    void findReceiversFcmToken() throws Exception {
        //given
        User sender = createUser("sender");
        Notification notification = createNotification(sender, POST_UPLOAD, createNotificationReceivers());
        em.flush();
        em.clear();

        //when
        List<String> fcmTokens = notificationRepository.findReceiversFcmToken(notification.getId());

        //then
        assertThat(fcmTokens.size()).isEqualTo(notification.getReceivers().size());
        assertThat(fcmTokens).allMatch(token -> token != null);
    }

    @Test
    @DisplayName("수신하지 않은 목표 완료 알림 조회")
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

    private List<NotificationReceiver> createNotificationReceivers() {
        User receiver1 = createUser("receiver1");
        User receiver2 = createUser("receiver2");
        return List.of(new NotificationReceiver(receiver1.getId()), new NotificationReceiver(receiver2.getId()));
    }

    private User createUser(String nickname) {
        User sender = TestEntityFactory.user(null, nickname);
        em.persist(sender);
        return sender;
    }
}