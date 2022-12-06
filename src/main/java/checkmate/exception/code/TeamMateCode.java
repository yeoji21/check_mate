package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamMateCode {
    NOT_FOUND("TM-001"),
    ALREADY_IN_GOAL("TM-002"),
    DUPLICATED_INVITE("TM-003"),
    ;

    private final String code;
}
