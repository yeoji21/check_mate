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
                .type(getType())
                .title("목표 수행 완료")
                .content(dto.goalTitle() + " 목표 수행을 끝까지 완수하였습니다")
                .receivers(List.of(new NotificationReceiver(dto.userId())))
                .build();
        notification.addAttribute("userId", dto.userId());
        notification.addAttribute("goalId", dto.goalId());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return COMPLETE_GOAL;
    }
}
