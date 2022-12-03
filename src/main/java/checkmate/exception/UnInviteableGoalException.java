package checkmate.exception;

import checkmate.exception.format.BusinessException;
import checkmate.exception.format.ErrorCode;

// TODO: 2022/12/04 예외 메시지 세분화
public class UnInviteableGoalException extends BusinessException {
    public UnInviteableGoalException() {
        super(ErrorCode.DUPLICATED_INVITE_REQUEST);
    }
}
