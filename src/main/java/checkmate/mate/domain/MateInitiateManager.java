package checkmate.mate.domain;

import checkmate.goal.domain.GoalJoiningPolicy;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MateInitiateManager {
    private final UserRepository userRepository;

    public void initiate(Mate mate) {
        int ongoingGoalCount = userRepository.countOngoingGoals(mate.getUserId());
        GoalJoiningPolicy.ongoingGoalCount(ongoingGoalCount);
        mate.toOngoingStatus();
    }
}
