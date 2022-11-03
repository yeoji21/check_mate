package checkmate.goal.domain;

import checkmate.exception.UserAlreadyInGoalException;

import static checkmate.exception.format.ErrorCode.ALREADY_IN_GOAL;
import static checkmate.exception.format.ErrorCode.WAITING_INVITE;

public enum TeamMateStatus {
    WAITING, ONGOING, REJECT, OUT, SUCCESS;

    void inviteeStatusCheck() {
        if(this == TeamMateStatus.ONGOING || this == TeamMateStatus.SUCCESS) throw new UserAlreadyInGoalException(ALREADY_IN_GOAL);
        else if(this == TeamMateStatus.WAITING) throw new UserAlreadyInGoalException(WAITING_INVITE);
    }
}
