package checkmate.notification.presentation.dto;

import checkmate.notification.application.dto.response.NotificationInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationInfoResult {
    private List<NotificationInfo> notifications;

    public NotificationInfoResult(List<NotificationInfo> notifications) {
        this.notifications = notifications;
    }
}
