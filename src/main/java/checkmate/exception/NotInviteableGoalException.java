package checkmate.exception;

import checkmate.exception.code.ErrorCode;


// TODO: 2023/06/07 클래스 명 변경 고려
/*
    1. 이미 목표에 속한 팀원인 경우
    2. 이미 초대 메시지를 보낸 경우
    3. 초대 기간이 지난 경우
 */
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