package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record CompleteGoalNotificationDto(
        long userId,
        long goalId,
        String goalTitle) implements NotificationCreateDto {

    @Override
    public long getSenderUserId() {
        return userId;
    }
}
