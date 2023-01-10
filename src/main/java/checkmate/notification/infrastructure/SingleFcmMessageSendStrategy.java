package checkmate.notification.infrastructure;

import checkmate.exception.RuntimeIOException;
import checkmate.exception.code.ErrorCode;
import checkmate.notification.domain.push.PushNotification;
import checkmate.notification.domain.push.PushNotificationSendStrategy;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class SingleFcmMessageSendStrategy implements PushNotificationSendStrategy<FcmSingleMessage> {
    private final WebClient webClient;
    private static final String TOKEN_PREFIX = "Bearer ";
    @Value("${fcm.single.uri}")
    private String URI;
    @Value("${fcm.single.endpoint}")
    private String FCM_SEND_ENDPOINT;
    @Value("${fcm.single.scope}")
    private String MESSAGING_SCOPE;
    private final String[] SCOPES = { MESSAGING_SCOPE };

    @Override
    public void send(FcmSingleMessage fcmSingleMessage) {
        webClient.post()
                .uri(URI + FCM_SEND_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fcmSingleMessage)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    throw new RuntimeIOException(ErrorCode.NOTIFICATION_PUSH_IO);
                })
                .bodyToMono(String.class)
                .subscribe(response -> log.info("firebase message send response : {}", response));
    }

    public Class<? extends PushNotification> getMessageType() {
        return FcmSingleMessage.class;
    }

    private String getAccessToken(){
        try {
            GoogleCredential googleCredential = GoogleCredential
                    .fromStream(new ClassPathResource("firebase/firebase_service_key.json").getInputStream())
                    .createScoped(Arrays.asList(SCOPES));
            googleCredential.refreshToken();
            return googleCredential.getAccessToken();
        } catch (IOException e) {
            throw new RuntimeIOException(e, ErrorCode.NOTIFICATION_PUSH_IO);
        }
    }
}
