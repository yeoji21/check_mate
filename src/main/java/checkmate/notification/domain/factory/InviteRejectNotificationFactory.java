package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InviteRejectNotificationFactory extends NotificationFactory<InviteRejectNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_REJECT;
    }

    @Override
    String getContent(InviteRejectNotificationDto dto) {
        return dto.inviteeNickname() + "님이 " + dto.goalTitle() + "목표로 합류를 거절했어요";
    }

    @Override
    List<NotificationReceiver> getReceivers(InviteRejectNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviterUserId()));
    }

    @Override
    void setAttributes(Notification notification, InviteRejectNotificationDto dto) {
        notification.addAttribute(NotificationAttributeKey.GOAL_ID, dto.goalId());
    }
}
