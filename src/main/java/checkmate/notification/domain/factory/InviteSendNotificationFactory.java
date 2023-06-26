package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.InviteSendNotificationDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InviteSendNotificationFactory extends NotificationFactory<InviteSendNotificationDto> {

    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_SEND;
    }

    @Override
    String getContent(InviteSendNotificationDto dto) {
        return dto.inviterNickname() + "님이 " + dto.goalTitle() + " 목표로 초대했습니다!";
    }

    @Override
    List<NotificationReceiver> getReceivers(InviteSendNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviteeUserId()));
    }

    @Override
    void setAttributes(Notification notification, InviteSendNotificationDto dto) {
        notification.addAttribute(NotificationAttributeKey.MATE_ID, dto.inviteeMateId());
    }
}
