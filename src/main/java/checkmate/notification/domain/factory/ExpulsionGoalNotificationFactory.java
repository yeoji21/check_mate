package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpulsionGoalNotificationFactory extends NotificationFactory<ExpulsionGoalNotificationDto> {

    @Override
    public Notification generate(ExpulsionGoalNotificationDto dto) {
        Notification notification = Notification.builder()
                .userId(dto.userId())
                .title("목표 퇴출 알림")
                .body(dto.goalTitle() + " 목표에서 퇴출되었습니다.")
                .build();
        notification.addAttribute("teamMateId", dto.teamMateId());
        notification.setUpReceivers(List.of(new NotificationReceiver(dto.userId())));
        notification.setNotificationType(getType());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EXPULSION_GOAL;
    }
}
