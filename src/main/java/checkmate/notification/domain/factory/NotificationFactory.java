package checkmate.notification.domain.factory;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;

public abstract class NotificationFactory<T> {
    public abstract Notification generate(T t);
    public abstract NotificationType getType();
}