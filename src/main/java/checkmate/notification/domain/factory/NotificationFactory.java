package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import java.util.List;

public abstract class NotificationFactory<D extends NotificationCreateDto> {

    final Notification generate(D dto) {
        Notification notification = Notification.builder()
            .userId(dto.getSenderUserId())
            .type(getType())
            .title(getType().getTitle())
            .content(getContent(dto))
            .receivers(getReceivers(dto))
            .build();
        setAttributes(notification, dto);
        return notification;
    }

    abstract NotificationType getType();

    abstract String getContent(D dto);

    abstract List<NotificationReceiver> getReceivers(D dto);

    abstract void setAttributes(Notification notification, D dto);
}