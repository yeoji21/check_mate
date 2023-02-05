package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationGenerator {
    private final Map<NotificationType, NotificationFactory<NotificationCreateDto>> factoryMap;

    public NotificationGenerator(List<NotificationFactory<NotificationCreateDto>> factories) {
        factoryMap = factories.stream()
                .collect(Collectors.toMap(NotificationFactory::getType, Function.identity()));
    }

    public Notification generate(NotificationType type, NotificationCreateDto dto) {
        return factoryMap.get(type).generate(dto);
    }
}
