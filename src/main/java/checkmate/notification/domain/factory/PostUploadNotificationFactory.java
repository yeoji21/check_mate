package checkmate.notification.domain.factory;


import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostUploadNotificationFactory extends NotificationFactory<PostUploadNotificationDto> {
    @Override
    public Notification generate(PostUploadNotificationDto dto) {
        Notification notification = Notification.builder()
                .userId(dto.uploaderUserId())
                .type(getType())
                .title("팀원의 목표인증")
                .body(dto.goalTitle() + " 목표의 " + dto.uploaderNickname() + "님이 목표 수행을 인증했어요!")
                .receivers(getReceivers(dto.teamMateUserIds()))
                .build();
        notification.addAttribute("goalId", dto.goalId());
        return notification;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.POST_UPLOAD;
    }

    private static List<NotificationReceiver> getReceivers(List<Long> teamMateUserIds) {
        return teamMateUserIds
                .stream()
                .map(NotificationReceiver::new)
                .collect(Collectors.toList());
    }
}
