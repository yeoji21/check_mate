package checkmate.mate.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.GoalPolicyConstants;
import checkmate.user.infrastructure.UserQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// TODO: 2023/08/15 클래스명
@RequiredArgsConstructor
@Component
public class MateStartingService {

    private final UserQueryDao userQueryDao;

    public void startToGoal(Mate mate) {
        if (isOverOngoingGoalLimit(mate)) {
            throw new BusinessException(ErrorCode.EXCEED_GOAL_LIMIT);
        }
        mate.acceptInvite();
    }

    private boolean isOverOngoingGoalLimit(Mate mate) {
        return userQueryDao.countOngoingGoals(mate.getUserId())
            >= GoalPolicyConstants.ONGOING_GOAL_COUNT_LIMIT;
    }
}
