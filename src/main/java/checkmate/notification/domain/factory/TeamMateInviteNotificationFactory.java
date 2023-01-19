package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeamMateInviteNotificationFactory extends NotificationFactory<TeamMateInviteNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL;
    }

    @Override
    String getContent(TeamMateInviteNotificationDto dto) {
        return dto.inviterNickname() + "님이 " + dto.goalTitle() + " 목표로 초대했습니다!";
    }

    @Override
    List<NotificationReceiver> getReceivers(TeamMateInviteNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviteeUserId()));
    }

    @Override
    void setAttributes(Notification notification, TeamMateInviteNotificationDto dto) {
        notification.addAttribute("teamMateId", dto.inviteeTeamMateId());
    }
}
