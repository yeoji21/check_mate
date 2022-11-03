package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.GoalCompleteNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;

@Component
public class GoalCompleteNotificationFactory extends NotificationFactory<GoalCompleteNotificationDto> {
    @Override
    public Notification generate(GoalCompleteNotificationDto dto) {
        Notification notification = Notification.builder()
                .userId(dto.getUserId())
                .title("목표 수행 완료")
                .body(dto.getGoalTitle() + " 목표 수행을 끝까지 완수하였습니다")
                .build();
        notification.addAttribute("userId", dto.getUserId());
        notification.addAttribute("goalId", dto.getGoalId());
        notification.setUpReceivers(List.of(new NotificationReceiver(dto.getUserId())));
        notification.setNotificationType(getType());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return COMPLETE_GOAL;
    }
}
