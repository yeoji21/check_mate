package checkmate.notification.domain.factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/*
- goalTitle, goalId
- userId
 */
@Getter
@Builder
@AllArgsConstructor
public class GoalCompleteNotificationDto {
    private long userId;
    private long goalId;
    private String goalTitle;
}
