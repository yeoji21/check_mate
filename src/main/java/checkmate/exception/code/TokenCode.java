package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenCode{
    NOT_FOUND("TOKEN-001"),
    REFRESH_TOKEN_EXPIRED("TOKEN-002"),
    ;

    private final String code;
}
