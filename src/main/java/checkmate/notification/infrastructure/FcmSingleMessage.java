package checkmate.notification.infrastructure;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.push.PushNotification;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class FcmSingleMessage implements PushNotification {
    private Message message;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Message {
        private Data data;
        private String token;
    }

    public static FcmSingleMessage getMessage(Notification notification, String fcmToken) {
        Data data = Data.builder()
                .body(notification.getContent())
                .title(notification.getTitle())
                .type(notification.getType().name())
                .notificationId(String.valueOf(notification.getId()))
                .build();

        return FcmSingleMessage.builder()
                .message(Message.builder()
                        .token(fcmToken)
                        .data(data)
                        .build())
                .build();
    }
}