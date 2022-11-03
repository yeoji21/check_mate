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
                .userId(dto.getInviteeUserId())
                .title("초대 응답")
                .body(getBody(dto))
                .build();
        notification.setUpReceivers(List.of(new NotificationReceiver(dto.getInviterUserId())));
        notification.addAttribute("goalId", dto.getGoalId());
        notification.addAttribute("accept", String.valueOf(dto.isAccept()));
        notification.setNotificationType(getType());
        return notification;
    }

    private static String getBody(InviteReplyNotificationDto command) {
        String body = command.getInviteeNickname() + "님이 " + command.getGoalTitle();
        body += command.isAccept() ? "목표로 합류했어요!" : "목표로 합류를 거절했어요";
        return body;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL_REPLY;
    }
}
