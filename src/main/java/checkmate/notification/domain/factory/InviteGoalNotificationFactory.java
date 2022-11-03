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
    public Notification generate(InviteGoalNotificationDto dto) {
        Notification notification = Notification.builder()
                .userId(dto.getInviterUserId())
                .title("팀원 초대")
                .body(dto.getInviterNickname() + "님이 " + dto.getGoalTitle() + " 목표로 초대했습니다!")
                .build();
        notification.addAttribute("teamMateId", dto.getInviteeTeamMateId());
        notification.setUpReceivers(List.of(new NotificationReceiver(dto.getInviteeUserId())));
        notification.setNotificationType(getType());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.INVITE_GOAL;
    }
}
