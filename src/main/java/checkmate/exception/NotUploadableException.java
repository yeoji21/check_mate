package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class NotUploadableException extends InvalidValueException {
    public NotUploadableException() {
        super(ErrorCode.CAN_NOT_UPLOADABLE);
    }
}
