package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;

public abstract class NotificationFactory<DTO extends NotificationCreateDto> {
    public abstract Notification generate(DTO dto);
    public abstract NotificationType getType();
}