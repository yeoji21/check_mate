package checkmate.user.presentation.dto.response;

import lombok.Builder;

@Builder
public record LoginTokenResponse(String accessToken,
                                 String refreshToken) {
}
