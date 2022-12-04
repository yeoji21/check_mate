package checkmate.goal.domain;

import checkmate.exception.UnInviteableGoalException;

public enum TeamMateStatus {
    WAITING, ONGOING, REJECT, OUT, SUCCESS;

    void inviteeStatusCheck() {
        if(this == TeamMateStatus.ONGOING || this == TeamMateStatus.SUCCESS)
            throw UnInviteableGoalException.ALREADY_IN_GOAL;
        else if(this == TeamMateStatus.WAITING)
            throw UnInviteableGoalException.DUPLICATED_INVITE_REQUEST;
    }
}
