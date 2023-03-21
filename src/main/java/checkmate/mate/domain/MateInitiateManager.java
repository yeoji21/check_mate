package checkmate.mate.domain;

import checkmate.goal.domain.GoalJoiningPolicy;
import checkmate.user.infrastructure.UserQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MateInitiateManager {
    private final UserQueryDao userQueryDao;

    public void initiate(Mate mate) {
        int ongoingGoalCount = userQueryDao.countOngoingGoals(mate.getUserId());
        GoalJoiningPolicy.ongoingGoalCount(ongoingGoalCount);
        mate.toOngoingStatus();
    }
}
