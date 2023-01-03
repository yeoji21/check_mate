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
    public NotificationType getType() {
        return NotificationType.EXPULSION_GOAL;
    }

    @Override
    String getContent(ExpulsionGoalNotificationDto dto) {
        return dto.goalTitle() + " 목표에서 퇴출되었습니다.";
    }

    @Override
    List<NotificationReceiver> getReceivers(ExpulsionGoalNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.userId()));
    }

    @Override
    void setAttributes(Notification notification, ExpulsionGoalNotificationDto dto) {
        notification.addAttribute("teamMateId", dto.teamMateId());
    }
}
