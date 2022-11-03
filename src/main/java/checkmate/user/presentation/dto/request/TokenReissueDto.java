package checkmate.user.presentation.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
public class TokenReissueDto {
    private String refreshToken;
    private String accessToken;

    @Builder
    public TokenReissueDto(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
