package checkmate.goal.domain;

import checkmate.exception.UnInviteableGoalException;
import checkmate.exception.BusinessException;

import static checkmate.exception.ErrorCode.ALREADY_IN_GOAL;

public enum TeamMateStatus {
    WAITING, ONGOING, REJECT, OUT, SUCCESS;

    void inviteeStatusCheck() {
        if(this == TeamMateStatus.ONGOING || this == TeamMateStatus.SUCCESS)
            throw new BusinessException(ALREADY_IN_GOAL);
        else if(this == TeamMateStatus.WAITING)
            throw new UnInviteableGoalException();
    }
}
