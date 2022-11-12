package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;

@Component
public class CompleteGoalNotificationFactory extends NotificationFactory<CompleteGoalNotificationDto> {
    @Override
    public Notification generate(CompleteGoalNotificationDto dto) {
        Notification notification = Notification.builder()
                .userId(dto.userId())
                .title("목표 수행 완료")
                .body(dto.goalTitle() + " 목표 수행을 끝까지 완수하였습니다")
                .build();
        notification.addAttribute("userId", dto.userId());
        notification.addAttribute("goalId", dto.goalId());
        notification.setUpReceivers(List.of(new NotificationReceiver(dto.userId())));
        notification.setNotificationType(getType());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return COMPLETE_GOAL;
    }
}
