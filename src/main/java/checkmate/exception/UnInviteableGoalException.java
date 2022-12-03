package checkmate.exception;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.InvalidValueException;

public class UnInviteableGoalException extends InvalidValueException {
    public UnInviteableGoalException() {
        super(ErrorCode.DUPLICATED_INVITE_REQUEST);
    }
}
