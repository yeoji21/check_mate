package checkmate.goal.domain;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {
    Goal save(Goal goal);
    Optional<Goal> findById(long goalId);
    List<Goal> findOngoingGoalList(Long userId);
    Optional<Goal> findByIdForUpdate(long goalId);
    boolean checkUserIsInGoal(long goalId, long userId);
    List<Goal> updateYesterdayOveredGoals();
    List<VerificationCondition> findConditions(Long goalId);
    int countOngoingGoals(long userId);
}
