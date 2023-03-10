package checkmate.notification.application;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.infrastructure.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationCommandService {
    private final NotificationRepository notificationRepository;
    private final NotificationGenerator notificationGenerator;
    private final NotificationSender notificationSender;

    @Transactional
    public void savePushNotification(NotificationType type, NotificationCreateDto dto) {
        Notification notification = notificationGenerator.generate(type, dto);
        notificationRepository.save(notification);
        List<String> tokens = notificationRepository.findReceiversFcmToken(notification.getId());
        notificationSender.send(notification, tokens);
    }

    @Transactional
    public void saveNotPushNotifications(NotificationType type, List<? extends NotificationCreateDto> dtos) {
        notificationRepository.saveAll(
                dtos.stream()
                        .map(dto -> notificationGenerator.generate(type, dto))
                        .toList()
        );
    }
}
