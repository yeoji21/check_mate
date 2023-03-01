package checkmate.notification.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationDetailResult {
    private List<NotificationDetailInfo> notifications;
    private boolean hasNext;

    @Builder
    public NotificationDetailResult(List<NotificationDetailInfo> notifications, boolean hasNext) {
        this.notifications = notifications;
        this.hasNext = hasNext;
    }
}
