package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.InviteAcceptNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InviteAcceptNotificationFactory extends NotificationFactory<InviteAcceptNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_ACCEPT;
    }

    @Override
    String getContent(InviteAcceptNotificationDto dto) {
        return dto.inviteeNickname() + "님이 " + dto.goalTitle() + "목표로 합류했어요!";
    }

    @Override
    List<NotificationReceiver> getReceivers(InviteAcceptNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviterUserId()));
    }

    @Override
    void setAttributes(Notification notification, InviteAcceptNotificationDto dto) {
        notification.addAttribute("goalId", dto.goalId());
    }
}
