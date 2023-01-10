package checkmate.notification.application;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.infrastructure.PushNotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationPushService {
    private final NotificationRepository notificationRepository;
    private final NotificationGenerator notificationGenerator;
    private final PushNotificationSender pushNotificationSender;

    @Transactional
    public void push(NotificationType type, NotificationCreateDto dto) {
        Notification notification = notificationGenerator.generate(type, dto);
        notificationRepository.save(notification);
        List<String> tokens = notificationRepository.findReceiversFcmToken(notification.getId());
        pushNotificationSender.send(notification, tokens);
    }

    @Transactional
    public void notPush(NotificationType type, List<? extends NotificationCreateDto> dtoList) {
        notificationRepository.saveAll(
                dtoList.stream()
                        .map(dto -> notificationGenerator.generate(type, dto))
                        .toList()
        );
    }
}
