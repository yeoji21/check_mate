package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record ExpulsionGoalNotificationDto(
        long userId,
        long mateId,
        String goalTitle) implements NotificationCreateDto {
    @Override
    public long getSenderUserId() {
        return userId;
    }
}
