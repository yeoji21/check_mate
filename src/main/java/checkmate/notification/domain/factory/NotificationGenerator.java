package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationGenerator {
    private final Map<NotificationType, NotificationFactory> factoryMap = new HashMap<>();

    public NotificationGenerator(List<NotificationFactory> factories) {
        for (NotificationFactory factory : factories) {
            factoryMap.put(factory.getType(), factory);
        }
    }

    public Notification generate(NotificationType type, NotificationCreateDto dto) {
        return factoryMap.get(type).generate(dto);
    }
}
