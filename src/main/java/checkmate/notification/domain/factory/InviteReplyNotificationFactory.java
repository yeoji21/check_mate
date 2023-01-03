package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.InviteReplyNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InviteReplyNotificationFactory extends NotificationFactory<InviteReplyNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL_REPLY;
    }

    @Override
    String getContent(InviteReplyNotificationDto dto) {
        StringBuilder content = new StringBuilder(dto.inviteeNickname() + "님이 " + dto.goalTitle());
        content.append(dto.accept() ? "목표로 합류했어요!" : "목표로 합류를 거절했어요");
        return content.toString();
    }

    @Override
    List<NotificationReceiver> getReceivers(InviteReplyNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviterUserId()));
    }

    @Override
    void setAttributes(Notification notification, InviteReplyNotificationDto dto) {
        notification.addAttribute("goalId", dto.goalId());
        notification.addAttribute("accept", String.valueOf(dto.accept()));
    }
}
