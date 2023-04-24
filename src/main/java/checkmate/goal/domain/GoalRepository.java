package checkmate.goal.domain;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {
    Goal save(Goal goal);

    Optional<Goal> findById(long goalId);

    Optional<Goal> findByIdForUpdate(long goalId);

    List<Long> updateYesterdayOveredGoals();

    Optional<Goal> findWithConditions(long goalId);

    void updateTodayStartGoal();
}
