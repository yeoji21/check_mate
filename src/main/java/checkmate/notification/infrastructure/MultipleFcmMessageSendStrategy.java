package checkmate.notification.infrastructure;

import checkmate.exception.ErrorCode;
import checkmate.exception.RuntimeIOException;
import checkmate.notification.domain.push.PushNotification;
import checkmate.notification.domain.push.PushNotificationSendStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class MultipleFcmMessageSendStrategy implements PushNotificationSendStrategy<FcmMultipleMessage> {
    private final WebClient webClient;
    private static final String TOKEN_PREFIX = "Bearer ";
    @Value("${fcm.multi.uri}")
    private String REQUEST_URI;
    @Value("${fcm.multi.token}")
    private String REQUEST_TOKEN;

    @Override
    public void send(FcmMultipleMessage fcmMultipleMessage) {
        if (fcmMultipleMessage.getRegistration_ids().size() == 0) return;
        webClient.post()
                .uri(REQUEST_URI)
                .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + REQUEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fcmMultipleMessage)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    throw new RuntimeIOException(ErrorCode.NOTIFICATION_PUSH_IO);
                })
                .bodyToMono(String.class)
                .subscribe(response -> log.info("firebase message send response : {}", response));
    }

    @Override
    public Class<? extends PushNotification> getMessageType() {
        return FcmMultipleMessage.class;
    }
}
