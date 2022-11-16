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
                .type(getType())
                .title("목표 퇴출 알림")
                .body(dto.goalTitle() + " 목표에서 퇴출되었습니다.")
                .receivers(List.of(new NotificationReceiver(dto.userId())))
                .build();
        notification.addAttribute("teamMateId", dto.teamMateId());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EXPULSION_GOAL;
    }
}
