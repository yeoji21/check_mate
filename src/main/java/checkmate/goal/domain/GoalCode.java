package checkmate.goal.domain;

import checkmate.exception.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalCode implements BusinessCode {
    NOT_FOUND("GOAL-001", "해당 목표를 찾을 수 없습니다."),
    COUNT_LIMIT("GOAL-002", "동시 진행 가능한 목표 수 허용치를 초과하였습니다."),
    DATE_RANGE("GOAL-003", "올바르지 않은 목표 기간 설정입니다."),
    WEEK_DAYS("GOAL-004", "올바르지 않은 인증 요일입니다.")
    ;

    private final String code;
    private final String message;
}
