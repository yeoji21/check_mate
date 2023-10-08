package checkmate.mate.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.GoalPolicyConstants;
import lombok.Getter;

@Getter
public class OngoingGoalCount {

    private final int count;

    public OngoingGoalCount(int count) {
        if (count >= GoalPolicyConstants.ONGOING_GOAL_COUNT_LIMIT) {
            throw new BusinessException(ErrorCode.EXCEED_GOAL_COUNT_LIMIT);
        }
        this.count = count;
    }
}
