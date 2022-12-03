package checkmate.goal.domain;

import checkmate.exception.format.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamMateCode implements BusinessCode {
    NOT_FOUND("TM-001", "해당 팀원을 찾을 수 없습니다."),
    ALREADY_IN_GOAL("TM-002", "이미 해당 목표를 진행 중입니다."),
    DUPLICATED_INVITE("TM-003", "이미 초대 요청을 보냈습니다."),

    ;

    private final String code;
    private final String message;
}
