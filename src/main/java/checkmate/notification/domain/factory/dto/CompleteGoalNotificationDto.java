package checkmate.notification.domain.factory.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompleteGoalNotificationDto implements NotificationCreateDto {
    private long userId;
    private long goalId;
    private String goalTitle;

    @QueryProjection
    @Builder
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