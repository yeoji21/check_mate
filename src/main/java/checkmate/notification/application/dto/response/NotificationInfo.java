package checkmate.notification.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationInfo {
    private String title;
    private String body;
    private String notificationType;
    private String attributes;

    @Builder
    public NotificationInfo(String title,
                            String body,
                            String notificationType,
                            String attributes) {
        this.title = title;
        this.body = body;
        this.notificationType = notificationType;
        this.attributes = attributes;
    }
}
