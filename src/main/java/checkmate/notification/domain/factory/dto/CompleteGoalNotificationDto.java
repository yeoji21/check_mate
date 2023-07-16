package checkmate.notification.domain.factory.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CompleteGoalNotificationDto implements NotificationCreateDto {

    private final long userId;
    private final long goalId;
    private final String goalTitle;

    @QueryProjection
    public CompleteGoalNotificationDto(long userId,
        long goalId,
        String goalTitle) {
        this.userId = userId;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
    }

    @Override
    public long getSenderUserId() {
        return userId;
    }
}