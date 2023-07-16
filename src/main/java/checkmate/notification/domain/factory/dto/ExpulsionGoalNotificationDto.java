package checkmate.notification.domain.factory.dto;

public record ExpulsionGoalNotificationDto(
    long userId,
    long mateId,
    String goalTitle) implements NotificationCreateDto {

    @Override
    public long getSenderUserId() {
        return userId;
    }
}
