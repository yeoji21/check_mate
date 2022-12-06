package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserCode{
    NOT_FOUND("USER-001"),
    EMPTY_NICKNAME("USER-002"),
    DUPLICATED_NICKNAME("USER-003");

    private final String code;
}
