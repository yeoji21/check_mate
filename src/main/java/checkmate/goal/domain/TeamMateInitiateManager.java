package checkmate.goal.domain;

import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TeamMateInitiateManager {
    private final UserRepository userRepository;

    public void initiate(TeamMate teamMate) {
        int ongoingGoalCount = userRepository.countOngoingGoals(teamMate.getUserId());
        GoalJoiningPolicy.ongoingGoalCount(ongoingGoalCount);
        teamMate.toOngoingStatus();
    }
}
