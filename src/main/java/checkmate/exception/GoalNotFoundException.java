package checkmate.exception;

import checkmate.exception.format.NotFoundException;
import checkmate.exception.format.ErrorCode;

public class GoalNotFoundException extends NotFoundException {
    public GoalNotFoundException() {
        super(ErrorCode.GOAL_NOT_FOUND);
    }
}
