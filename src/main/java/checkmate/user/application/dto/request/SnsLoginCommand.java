package checkmate.user.application.dto.request;

import lombok.*;

@Builder
public record SnsLoginCommand(
    String providerId,
    String fcmToken) {
}
