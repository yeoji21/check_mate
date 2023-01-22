package checkmate.user.application.dto.request;

import lombok.Builder;

@Builder
public record TokenReissueCommand (
    String refreshToken,
    String accessToken) {
}
