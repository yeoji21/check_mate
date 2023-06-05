package checkmate.mate.domain;

import static checkmate.exception.code.ErrorCode.INVALID_MATE_STATUS;

import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;

public enum MateStatus {
    CREATED,
    WAITING,
    ONGOING,
    REJECT,
    OUT,
    SUCCESS;

    void inviteableCheck() {
        if (this == MateStatus.ONGOING || this == MateStatus.SUCCESS) {
            throw UnInviteableGoalException.ALREADY_IN_GOAL;
        } else if (this == MateStatus.WAITING) {
            throw UnInviteableGoalException.DUPLICATED_INVITE_REQUEST;
        }
    }

    void initiateableCheck() {
        if (this != WAITING) {
            throw new BusinessException(INVALID_MATE_STATUS);
        }
    }
}
