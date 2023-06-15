package checkmate.exception;

import checkmate.exception.code.ErrorCode;


public class NotInviteableGoalException extends BusinessException {

    public static final NotInviteableGoalException ALREADY_IN_GOAL =
        new NotInviteableGoalException(ErrorCode.ALREADY_IN_GOAL);
    public static final NotInviteableGoalException DUPLICATED_INVITE =
        new NotInviteableGoalException(ErrorCode.DUPLICATED_INVITE);
    public static final NotInviteableGoalException EXCEED_INVITEABLE_DATE =
        new NotInviteableGoalException(ErrorCode.EXCEED_INVITEABLE_DATE);

    private NotInviteableGoalException(ErrorCode errorCode) {
        super(errorCode);
    }
}