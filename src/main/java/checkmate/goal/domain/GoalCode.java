package checkmate.goal.domain;

import checkmate.exception.format.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalCode implements BusinessCode {
    NOT_FOUND("GOAL-001", "해당 목표를 찾을 수 없습니다."),
    EXCEED_LIMIT("GOAL-002", "동시 진행 가능한 목표 수 허용치를 초과하였습니다.");


    private final String code;
    private final String message;
}
