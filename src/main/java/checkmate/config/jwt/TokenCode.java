package checkmate.config.jwt;

import checkmate.exception.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenCode implements BusinessCode {
    NOT_FOUND("TOKEN-001", "해당 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED("TOKEN-002", "만료된 Refresh Token을 사용할 수 없습니다."),
    ;

    private final String code;
    private final String message;
}
