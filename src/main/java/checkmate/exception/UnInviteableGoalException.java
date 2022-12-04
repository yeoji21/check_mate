package checkmate.exception;

/*
    1. 이미 목표에 속한 팀원인 경우
    2. 이미 초대 메시지를 보낸 경우
    3. 초대 기간이 지난 경우
 */
public class UnInviteableGoalException extends BusinessException {
    public static final UnInviteableGoalException ALREADY_IN_GOAL = new UnInviteableGoalException(ErrorCode.ALREADY_IN_GOAL);
    public static final UnInviteableGoalException DUPLICATED_INVITE_REQUEST = new UnInviteableGoalException(ErrorCode.DUPLICATED_INVITE_REQUEST);
    public static final UnInviteableGoalException EXCEED_GOAL_INVITEABLE_DATE = new UnInviteableGoalException(ErrorCode.EXCEED_GOAL_INVITEABLE_DATE);

    private UnInviteableGoalException(ErrorCode errorCode) {
        super(errorCode);
    }
}
