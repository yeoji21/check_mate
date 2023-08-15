package checkmate.goal.domain;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {

    Goal save(Goal goal);

    void saveCondition(VerificationCondition condition);

    Optional<Goal> find(long goalId);

    Optional<Goal> findForUpdate(long goalId);

    List<VerificationCondition> findConditions(long goalId);

    void updateStatusToOver(List<Long> goalIds);

    void updateTodayStartGoalsToOngoing();
}
