package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class UpdateDurationException extends InvalidValueException {
    public UpdateDurationException() {
        super(ErrorCode.UPDATE_DURATION);
    }
}
