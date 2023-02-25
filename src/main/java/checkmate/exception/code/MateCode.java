package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MateCode {
    NOT_FOUND("MATE-001"),
    ALREADY_IN_GOAL("MATE-002"),
    DUPLICATED_INVITE("MATE-003"),
    INVALID_STATUS("MATE-004");

    private final String code;
}
