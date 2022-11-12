package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record KickOutNotificationDto (
    long userId,
    long teamMateId,
    String goalTitle) implements NotificationCreateDto{
}
