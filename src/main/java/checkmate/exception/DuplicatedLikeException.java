package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class DuplicatedLikeException extends InvalidValueException {
    public DuplicatedLikeException() {super(ErrorCode.DUPLICATED_LIKE);}
}
