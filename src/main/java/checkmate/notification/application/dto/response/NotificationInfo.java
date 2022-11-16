package checkmate.notification.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationInfo {
    private String title;
    private String body;
    private String type;
    private String attributes;

    @Builder
    public NotificationInfo(String title,
                            String body,
                            String type,
                            String attributes) {
        this.title = title;
        this.body = body;
        this.type = type;
        this.attributes = attributes;
    }
}
