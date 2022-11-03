package checkmate.notification.domain.event;

import checkmate.notification.domain.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class StaticNotificationCreatedEvent {
    private final NotificationType notificationType;
    private final List<?> createCommand;
}
