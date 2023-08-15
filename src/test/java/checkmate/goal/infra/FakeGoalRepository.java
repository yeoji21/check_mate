package checkmate.goal.infra;


import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.VerificationCondition;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeGoalRepository implements GoalRepository {

    private final AtomicLong goalId = new AtomicLong(1);
    private final Map<Long, Goal> goalMap = new HashMap<>();
    private final Map<Long, List<VerificationCondition>> conditionMap = new HashMap<>();

    @Override
    public Goal save(Goal goal) {
        ReflectionTestUtils.setField(goal, "id", goalId.getAndIncrement());
        goalMap.put(goal.getId(), goal);
        return goal;
    }

    @Override
    public void saveCondition(VerificationCondition condition) {
        conditionMap.put(condition.getGoalId(), List.of(condition));
    }

    @Override
    public Optional<Goal> find(long goalId) {
        return Optional.ofNullable(goalMap.get(goalId));
    }

    @Override
    public Optional<Goal> findForUpdate(long goalId) {
        return Optional.ofNullable(goalMap.get(goalId));
    }

    @Override
    public void updateStatusToOver(List<Long> goalIds) {
        goalIds.forEach(id -> {
            Goal goal = goalMap.get(id);
            ReflectionTestUtils.setField(goal, "status",
                Goal.GoalStatus.OVER);
        });
    }

    @Override
    public void updateTodayStartGoalsToOngoing() {
        goalMap.values().stream()
            .filter(goal -> goal.getStartDate() == LocalDate.now().minusDays(1))
            .forEach(goal -> ReflectionTestUtils.setField(goal, "status",
                Goal.GoalStatus.ONGOING));
    }

    @Override
    public List<VerificationCondition> findConditions(long goalId) {
        return conditionMap.get(goalId);
    }
}
