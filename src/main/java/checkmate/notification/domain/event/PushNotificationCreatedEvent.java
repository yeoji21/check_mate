package checkmate.notification.domain.event;

import checkmate.notification.domain.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PushNotificationCreatedEvent {
    private final NotificationType notificationType;
    private final Object createCommand;
}
