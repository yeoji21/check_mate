package checkmate.goal.domain;

import checkmate.exception.format.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamMateCode implements BusinessCode {
    NOT_FOUND("TM-001", "해당 팀원을 찾을 수 없습니다."),
    ;

    private final String code;
    private final String message;
}
