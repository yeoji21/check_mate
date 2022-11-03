package checkmate.user.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenReissueCommand {
    private String refreshToken;
    private String accessToken;

    @Builder
    TokenReissueCommand(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
