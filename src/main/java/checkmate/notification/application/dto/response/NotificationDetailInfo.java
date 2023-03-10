package checkmate.notification.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NotificationDetailInfo {
    private long notificationId;
    private String title;
    private String content;
    private boolean checked;
    private String sendAt;
    private String type;

    @Builder
    @QueryProjection
    public NotificationDetailInfo(long notificationId,
                                  String title,
                                  String content,
                                  boolean checked,
                                  LocalDateTime sendAt,
                                  String type) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.checked = checked;
        this.sendAt = sendAt.toString();
        this.type = type;
    }
}
