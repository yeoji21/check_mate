package checkmate.user.presentation.dto.response;

import checkmate.config.auth.AuthConstants;
import lombok.*;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginTokenResponse{
    private String refreshToken;
    private String accessToken;

    @Builder
    public LoginTokenResponse(String refreshToken, String accessToken) {
        this.refreshToken = AuthConstants.TOKEN_PREFIX + refreshToken;
        this.accessToken = AuthConstants.TOKEN_PREFIX + accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginTokenResponse that = (LoginTokenResponse) o;
        return Objects.equals(getRefreshToken(), that.getRefreshToken()) && Objects.equals(getAccessToken(), that.getAccessToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRefreshToken(), getAccessToken());
    }
}
