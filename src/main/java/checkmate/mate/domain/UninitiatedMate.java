package checkmate.mate.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.GoalPolicyConstants;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UninitiatedMate {

    private final Mate mate;
    private final int ongoingGoalCount;

    public void initiate() {
        if (this.ongoingGoalCount >= GoalPolicyConstants.ONGOING_GOAL_COUNT_LIMIT) {
            throw new BusinessException(ErrorCode.EXCEED_GOAL_LIMIT);
        }
        mate.acceptInvite();
    }
}
