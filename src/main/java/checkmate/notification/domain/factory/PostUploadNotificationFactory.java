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
                .userId(dto.getUploaderUserId())
                .title("팀원의 목표인증")
                .body(dto.getGoalTitle() + " 목표의 " + dto.getUploderNickname() + "님이 목표 수행을 인증했어요!")
                .build();

        notification.addAttribute("goalId", dto.getGoalId());
        notification.setUpReceivers(getNotificationReceivers(dto.getTeamMateUserIds()));
        notification.setNotificationType(NotificationType.POST_UPLOAD);
        return notification;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.POST_UPLOAD;
    }

    private static List<NotificationReceiver> getNotificationReceivers(List<Long> teamMateUserIds) {
        return teamMateUserIds
                .stream()
                .map(NotificationReceiver::new)
                .collect(Collectors.toList());
    }
}
