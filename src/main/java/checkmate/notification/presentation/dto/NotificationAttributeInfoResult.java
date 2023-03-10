package checkmate.notification.presentation.dto;

import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationAttributeInfoResult {
    private List<NotificationAttributeInfo> notifications;

    public NotificationAttributeInfoResult(List<NotificationAttributeInfo> notifications) {
        this.notifications = notifications;
    }
}
