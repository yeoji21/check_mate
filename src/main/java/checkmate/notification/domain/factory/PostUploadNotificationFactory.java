package checkmate.notification.domain.factory;


import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostUploadNotificationFactory extends NotificationFactory<PostUploadNotificationDto> {
    @Override
    public NotificationType getType() {
        return NotificationType.POST_UPLOAD;
    }

    @Override
    String getContent(PostUploadNotificationDto dto) {
        return dto.goalTitle() + " 목표의 " + dto.uploaderNickname() + "님이 목표 수행을 인증했어요!";
    }

    @Override
    List<NotificationReceiver> getReceivers(PostUploadNotificationDto dto) {
        return dto.mateUserIds()
                .stream()
                .map(NotificationReceiver::new)
                .collect(Collectors.toList());
    }

    @Override
    void setAttributes(Notification notification, PostUploadNotificationDto dto) {
        notification.addAttribute(NotificationAttributeKey.GOAL_ID, dto.goalId());
    }
}
