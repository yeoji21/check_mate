package checkmate.notification.domain.push;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public interface PushNotification {
    @Getter
    @Builder
    @AllArgsConstructor
    class Data {
        private String title;
        private String body;
        private String type;
        private String notificationId;
    }
}
