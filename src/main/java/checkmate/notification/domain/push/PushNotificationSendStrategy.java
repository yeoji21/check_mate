package checkmate.notification.domain.push;


public interface PushNotificationSendStrategy<T extends PushNotification> {
    void send(T t);
    Class<? extends PushNotification> getMessageType();
}
