package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalCode{
    NOT_FOUND("GOAL-001"),
    COUNT_LIMIT("GOAL-002"),
    DATE("GOAL-003"),
    WEEK_DAYS("GOAL-004"),
    INVITEABLE_DATE("GOAL-005"),
    ;

    private final String code;
}
