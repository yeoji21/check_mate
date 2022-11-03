package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class ExceedImageCountException extends InvalidValueException {
    public ExceedImageCountException() {
        super(ErrorCode.EXCEED_LIMIT);
    }
}
