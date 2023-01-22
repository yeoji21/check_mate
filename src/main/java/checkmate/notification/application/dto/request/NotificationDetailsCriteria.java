package checkmate.notification.application.dto.request;

import lombok.Builder;

@Builder
public record NotificationDetailsCriteria (
    long userId,
    Long cursorId,
    int size) {
}
