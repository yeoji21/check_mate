package checkmate.goal.domain;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {

    Goal save(Goal goal);

    void saveCondition(VerificationCondition condition);

    Optional<Goal> findById(long goalId);

    Optional<Goal> findByIdWithLock(long goalId);

    void updateStatusToOver(List<Long> goalIds);

    void updateTodayStartGoalsToOngoing();

    List<VerificationCondition> findConditions(long goalId);
}
