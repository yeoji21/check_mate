package checkmate.notification.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationDetailsResult {
    private List<NotificationDetails> notificationDetails;
    private boolean hasNext;

    @Builder
    public NotificationDetailsResult(List<NotificationDetails> notificationDetails, boolean hasNext) {
        this.notificationDetails = notificationDetails;
        this.hasNext = hasNext;
    }
}
