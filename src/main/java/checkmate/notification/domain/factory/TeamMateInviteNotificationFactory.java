package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.MateInviteNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeamMateInviteNotificationFactory extends NotificationFactory<MateInviteNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL;
    }

    @Override
    String getContent(MateInviteNotificationDto dto) {
        return dto.inviterNickname() + "님이 " + dto.goalTitle() + " 목표로 초대했습니다!";
    }

    @Override
    List<NotificationReceiver> getReceivers(MateInviteNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.inviteeUserId()));
    }

    @Override
    void setAttributes(Notification notification, MateInviteNotificationDto dto) {
        notification.addAttribute("mateId", dto.inviteeMateId());
    }
}
