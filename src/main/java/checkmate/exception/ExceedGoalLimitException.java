package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class ExceedGoalLimitException extends InvalidValueException {
    public ExceedGoalLimitException() {
        super(ErrorCode.EXCEED_LIMIT);
    }
}
