package checkmate.user.application.dto.request;

import lombok.*;

@Getter
public class SnsLoginCommand {
    private String providerId;
    private String fcmToken;

    @Builder
    public SnsLoginCommand(String providerId, String fcmToken) {
        this.providerId = providerId;
        this.fcmToken = fcmToken;
    }
}
