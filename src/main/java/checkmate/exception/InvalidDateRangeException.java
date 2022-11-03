package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class InvalidDateRangeException extends InvalidValueException {
    public InvalidDateRangeException() {
        super(ErrorCode.INVALID_DATE_RANGE);
    }
}
