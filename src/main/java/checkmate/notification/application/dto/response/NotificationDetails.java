package checkmate.notification.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NotificationDetails {
    private long notificationId;
    private String title;
    private String body;
    private boolean checked;
    private String sendAt;
    private String type;

    @Builder @QueryProjection
    public NotificationDetails(long notificationId,
                               String title,
                               String body,
                               boolean checked,
                               LocalDateTime sendAt,
                               String type) {
        this.notificationId = notificationId;
        this.title = title;
        this.body = body;
        this.checked = checked;
        this.sendAt = sendAt.toString();
        this.type = type;
    }
}
