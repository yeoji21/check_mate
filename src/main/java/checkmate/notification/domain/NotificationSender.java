package checkmate.notification.domain;

import java.util.List;

public interface NotificationSender {
    void send(Notification notification, List<String> tokens);
}
