package checkmate.mate.domain;

import checkmate.goal.domain.GoalJoiningPolicy;
import checkmate.user.infrastructure.UserQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MateStartingService {

    private final UserQueryDao userQueryDao;

    public void startToGoal(Mate mate) {
        int ongoingGoalCount = userQueryDao.countOngoingGoals(mate.getUserId());
        GoalJoiningPolicy.ongoingGoalCount(ongoingGoalCount);
        mate.acceptInvite();
    }
}
