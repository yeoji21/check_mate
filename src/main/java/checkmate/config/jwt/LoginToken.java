package checkmate.config.jwt;

import lombok.Builder;

@Builder
public record LoginToken(String accessToken,
                         String refreshToken) {
}
