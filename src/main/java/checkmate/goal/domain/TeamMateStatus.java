package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;

import static checkmate.exception.code.ErrorCode.INVALID_TEAM_MATE_STATUS;

public enum TeamMateStatus {
    WAITING, ONGOING, REJECT, OUT, SUCCESS;

    void inviteableCheck() {
        if (this == TeamMateStatus.ONGOING || this == TeamMateStatus.SUCCESS)
            throw UnInviteableGoalException.ALREADY_IN_GOAL;
        else if (this == TeamMateStatus.WAITING)
            throw UnInviteableGoalException.DUPLICATED_INVITE_REQUEST;
    }

    void initiateableCheck() {
        if (this != WAITING) throw new BusinessException(INVALID_TEAM_MATE_STATUS);
    }
}
