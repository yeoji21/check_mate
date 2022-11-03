package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class InvalidWeekDaysException extends InvalidValueException {
    public InvalidWeekDaysException() {
        super(ErrorCode.INVALID_WEEK_DAYS);
    }
}
