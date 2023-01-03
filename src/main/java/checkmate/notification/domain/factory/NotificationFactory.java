package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;

import java.util.Collections;

public abstract class NotificationFactory<DTO extends NotificationCreateDto> {
    Notification generate(DTO dto){
        Notification notification = Notification.builder()
                .userId(dto.getSenderUserId())
                .type(getType())
                .title("")
                .content("")
                .receivers(Collections.EMPTY_LIST)
                .build();
        return notification;
    }
    abstract NotificationType getType();
}