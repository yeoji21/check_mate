package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.InviteGoalNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InviteGoalNotificationFactory extends NotificationFactory<InviteGoalNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL;
    }

    @Override
    String getContent(InviteGoalNotificationDto dto) {
        return dto.inviterNickname() + "님이 " + dto.goalTitle() + " 목표로 초대했습니다!";
    }

    @Override
    List<NotificationReceiver> getReceivers(InviteGoalNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviteeUserId()));
    }

    @Override
    void setAttributes(Notification notification, InviteGoalNotificationDto dto) {
        notification.addAttribute("teamMateId", dto.inviteeTeamMateId());
    }
}
