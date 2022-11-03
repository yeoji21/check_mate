package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;


public class InvalidLikeCountException extends InvalidValueException {
    public InvalidLikeCountException() {
        super(ErrorCode.INVALID_MINIMUM_LIKE);
    }
}
