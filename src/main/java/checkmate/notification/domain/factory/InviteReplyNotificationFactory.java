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
    public Notification generate(InviteReplyNotificationDto dto) {
        Notification notification = Notification.builder()
                .userId(dto.inviteeUserId())
                .type(getType())
                .title("초대 응답")
                .body(getBody(dto))
                .receivers(List.of(new NotificationReceiver(dto.inviterUserId())))
                .build();
        notification.addAttribute("goalId", dto.goalId());
        notification.addAttribute("accept", String.valueOf(dto.accept()));
        return notification;
    }

    private static String getBody(InviteReplyNotificationDto command) {
        String body = command.inviteeNickname() + "님이 " + command.goalTitle();
        body += command.accept() ? "목표로 합류했어요!" : "목표로 합류를 거절했어요";
        return body;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL_REPLY;
    }
}
