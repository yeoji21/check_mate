package checkmate.notification.presentation.dto.response;

import checkmate.notification.application.dto.response.NotificationInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationInfosResult {
    List<NotificationInfo> notifications;

    public NotificationInfosResult(List<NotificationInfo> notifications) {
        this.notifications = notifications;
    }
}
