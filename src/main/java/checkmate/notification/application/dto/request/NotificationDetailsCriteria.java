package checkmate.notification.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationDetailsCriteria {
    private long userId;
    private Long cursorId;
    private int size;

    @Builder
    public NotificationDetailsCriteria(long userId, Long cursorId, int size) {
        this.userId = userId;
        this.cursorId = cursorId;
        this.size = size;
    }
}
