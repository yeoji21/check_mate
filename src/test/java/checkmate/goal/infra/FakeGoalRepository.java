package checkmate.goal.infra;


import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeGoalRepository implements GoalRepository {

    private final AtomicLong goalId = new AtomicLong(1);
    private final Map<Long, Goal> map = new HashMap<>();

    @Override
    public Goal save(Goal goal) {
        ReflectionTestUtils.setField(goal, "id", goalId.getAndIncrement());
        map.put(goal.getId(), goal);
        return goal;
    }

    @Override
    public Optional<Goal> findById(long goalId) {
        return Optional.ofNullable(map.get(goalId));
    }

    @Override
    public Optional<Goal> findByIdWithLock(long goalId) {
        return Optional.ofNullable(map.get(goalId));
    }

    @Override
    public Optional<Goal> findWithConditions(long goalId) {
        return Optional.ofNullable(map.get(goalId));
    }

    @Override
    public void updateStatusToOver(List<Long> goalIds) {
        goalIds.forEach(id -> {
            Goal goal = map.get(id);
            ReflectionTestUtils.setField(goal, "status",
                Goal.GoalStatus.OVER);
        });
    }

    @Override
    public void updateTodayStartGoalsToOngoing() {
        map.values().stream()
            .filter(goal -> goal.getStartDate() == LocalDate.now().minusDays(1))
            .forEach(goal -> ReflectionTestUtils.setField(goal, "status",
                Goal.GoalStatus.ONGOING));
    }
}
