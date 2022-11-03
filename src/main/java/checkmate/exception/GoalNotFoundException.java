package checkmate.exception;

import checkmate.exception.format.EntityNotFoundException;
import checkmate.exception.format.ErrorCode;

public class GoalNotFoundException extends EntityNotFoundException {
    public GoalNotFoundException() {
        super(ErrorCode.GOAL_NOT_FOUND);
    }
}
