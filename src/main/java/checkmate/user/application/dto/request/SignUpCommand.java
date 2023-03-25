package checkmate.user.application.dto.request;

import lombok.Builder;

@Builder
public record SignUpCommand(
        String providerId,
        String username,
        String emailAddress,
        String nickname,
        String fcmToken) {
}
