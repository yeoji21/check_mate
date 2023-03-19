package checkmate.user.presentation.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(String accessToken,
                            String refreshToken) {
}
