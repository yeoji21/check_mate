package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class DuplicatedNicknameException extends InvalidValueException {
    public DuplicatedNicknameException() {
        super(ErrorCode.DUPLICATED_NICKNAME);
    }
}
