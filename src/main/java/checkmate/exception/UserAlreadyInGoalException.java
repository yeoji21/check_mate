package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;


public class UserAlreadyInGoalException extends InvalidValueException {
    public UserAlreadyInGoalException(ErrorCode errorCode) {
        super(errorCode);
    }
}
