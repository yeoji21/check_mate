package checkmate.notification.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationAttributeInfo {
    private String title;
    private String content;
    private String type;
    private String attributes;

    @Builder
    public NotificationAttributeInfo(String title,
                                     String content,
                                     String type,
                                     String attributes) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.attributes = attributes;
    }
}
