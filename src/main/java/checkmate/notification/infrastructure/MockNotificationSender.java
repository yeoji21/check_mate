package checkmate.notification.infrastructure;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationSender;
import com.mysema.commons.lang.Assert;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("!production")
@Component
public class MockNotificationSender implements NotificationSender {
    @Override
    public void send(Notification notification, List<String> tokens) {
        Assert.notNull(notification, "notification must not be null");
        Assert.isTrue(tokens.size() > 0, "tokens must not be empty");
    }
}
