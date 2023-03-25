package checkmate.user.application.dto.request;

import lombok.Builder;

@Builder
public record LoginCommand(
        String identifier,
        String fcmToken) {
}
