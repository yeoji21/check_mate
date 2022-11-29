package checkmate.goal.domain;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {
    Goal save(Goal goal);
    Optional<Goal> findById(long goalId);
    Optional<Goal> findByIdForUpdate(long goalId);
    boolean checkUserIsInGoal(long goalId, long userId);
    List<Long> updateYesterdayOveredGoals();
    Optional<Goal> findWithConditions(Long goalId);
    int countOngoingGoals(long userId);
    void updateTodayStartGoal();
}
