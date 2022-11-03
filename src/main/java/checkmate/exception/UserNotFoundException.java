package checkmate.exception;

import checkmate.exception.format.EntityNotFoundException;
import checkmate.exception.format.ErrorCode;


public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
