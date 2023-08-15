package checkmate.mate.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.GoalPolicyConstants;
import lombok.Getter;

public class UninitiatedMate {

    private final Mate mate;
    @Getter
    private final int ongoingGoalCount;

    public UninitiatedMate(Mate mate, int ongoingGoalCount) {
        this.mate = mate;
        this.ongoingGoalCount = ongoingGoalCount;
    }

    public void initiate() {
        if (this.ongoingGoalCount >= GoalPolicyConstants.ONGOING_GOAL_COUNT_LIMIT) {
            throw new BusinessException(ErrorCode.EXCEED_GOAL_LIMIT);
        }
        mate.acceptInvite();
    }
}
