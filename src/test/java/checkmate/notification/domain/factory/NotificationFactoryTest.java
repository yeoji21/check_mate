package checkmate.notification.domain.factory;

import static checkmate.notification.domain.NotificationAttributeKey.GOAL_ID;
import static checkmate.notification.domain.NotificationAttributeKey.MATE_ID;
import static checkmate.notification.domain.NotificationAttributeKey.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteAcceptNotificationDto;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.InviteSendNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationFactoryTest {


    @Test
    void generateCompleteGoalNotification() throws Exception {
        //given
        NotificationCreateDto dto = new CompleteGoalNotificationDto(1L, 1L, "title");
        NotificationFactory factory = new CompleteGoalNotificationFactory();
        //when
        Notification notification = factory.generate(dto);
        //then
        assertThat(notification.getType()).isEqualTo(NotificationType.COMPLETE_GOAL);
        assertThat(notification.getLongAttribute(USER_ID)).isPositive();
        assertThat(notification.getLongAttribute(GOAL_ID)).isPositive();
    }

    @Test
    void generateExpulsionGoalNotification() throws Exception {
        //given
        ExpulsionGoalNotificationDto dto = new ExpulsionGoalNotificationDto(1L, 1L, "title");
        NotificationFactory factory = new ExpulsionGoalNotificationFactory();
        //when
        Notification notification = factory.generate(dto);
        //then
        assertThat(notification.getType()).isEqualTo(NotificationType.EXPULSION_GOAL);
        assertThat(notification.getLongAttribute(MATE_ID)).isPositive();
    }

    @Test
    void generateInviteAcceptNotification() throws Exception {
        //given
        InviteAcceptNotificationDto dto = new InviteAcceptNotificationDto(
            1L, "invitee", 2L, "title", 3L);
        NotificationFactory factory = new InviteAcceptNotificationFactory();
        //when
        Notification notification = factory.generate(dto);
        //then
        assertThat(notification.getType()).isEqualTo(NotificationType.INVITE_ACCEPT);
        assertThat(notification.getLongAttribute(GOAL_ID)).isPositive();
    }

    @Test
    void generateInviteRejectNotification() throws Exception {
        InviteRejectNotificationDto dto = new InviteRejectNotificationDto(
            1L, "invitee", 2L, "title", 3L);
        NotificationFactory factory = new InviteRejectNotificationFactory();
        //when
        Notification notification = factory.generate(dto);
        //then
        assertThat(notification.getType()).isEqualTo(NotificationType.INVITE_REJECT);
        assertThat(notification.getLongAttribute(GOAL_ID)).isPositive();
    }

    @Test
    void generateInviteSendNotification() throws Exception {
        InviteSendNotificationDto dto = new InviteSendNotificationDto(1L,
            "inviter", "title", 2L, 3L);
        NotificationFactory factory = new InviteSendNotificationFactory();
        //when
        Notification notification = factory.generate(dto);
        //then
        assertThat(notification.getType()).isEqualTo(NotificationType.INVITE_SEND);
        assertThat(notification.getLongAttribute(MATE_ID)).isPositive();
    }

    @Test
    void generatePostUploadNotification() throws Exception {
        PostUploadNotificationDto dto = new PostUploadNotificationDto(1L,
            "title", 2L, "title");
        dto.setMateUserIds(List.of(1L, 2L, 3L));
        NotificationFactory factory = new PostUploadNotificationFactory();
        //when
        Notification notification = factory.generate(dto);
        //then
        assertThat(notification.getType()).isEqualTo(NotificationType.POST_UPLOAD);
        assertThat(notification.getLongAttribute(GOAL_ID)).isPositive();
    }
}