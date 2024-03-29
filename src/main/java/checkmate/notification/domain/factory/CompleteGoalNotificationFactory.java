package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;

@Component
public class CompleteGoalNotificationFactory extends NotificationFactory<CompleteGoalNotificationDto> {
    @Override
    public NotificationType getType() {
        return COMPLETE_GOAL;
    }

    @Override
    String getContent(CompleteGoalNotificationDto dto) {
        return dto.getGoalTitle() + " 목표 수행을 끝까지 완수하였습니다";
    }

    @Override
    List<NotificationReceiver> getReceivers(CompleteGoalNotificationDto dto) {
        return List.of(new NotificationReceiver(dto.getUserId()));
    }

    @Override
    void setAttributes(Notification notification, CompleteGoalNotificationDto dto) {
        notification.addAttribute(NotificationAttributeKey.USER_ID, dto.getUserId());
        notification.addAttribute(NotificationAttributeKey.GOAL_ID, dto.getGoalId());
    }
}
