package checkmate.notification.infrastructure;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.push.PushNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;


@Builder
@AllArgsConstructor
@lombok.Data
public class FcmMultipleMessage implements PushNotification {
    private Data data;
    private List<String> registration_ids;

    public static FcmMultipleMessage getMessages(Notification notification, List<String> fcmTokens) {
        Data data = Data.builder()
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getNotificationType().name())
                .notificationId(String.valueOf(notification.getId()))
                .build();

        return FcmMultipleMessage.builder()
                .registration_ids(fcmTokens)
                .data(data)
                .build();
    }
}
