package checkmate.mate.infra;

import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.OngoingGoalCount;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeMateRepository implements MateRepository {

    private final AtomicLong mateId = new AtomicLong(1);
    private final Map<Long, Mate> map = new HashMap<>();

    @Override
    public Optional<Mate> findById(long mateId) {
        return Optional.ofNullable(map.get(mateId));
    }

    @Override
    public Optional<Mate> findWithGoal(long mateId) {
        return Optional.ofNullable(map.get(mateId));
    }

    @Override
    public Optional<Mate> findWithGoal(long goalId, long userId) {
        return map.values().stream()
            .filter(mate -> mate.getGoal().getId() == goalId && mate.getUserId() == userId)
            .findAny();
    }

    @Override
    public void increaseSkippedDayCount(List<Mate> mates) {
        mates.forEach(mate -> ReflectionTestUtils.setField(mate.getAttendance(), "skippedDayCount",
            mate.getSkippedDayCount() + 1));
    }

    @Override
    public void updateLimitOveredMates(List<Mate> limitOveredMates) {
        limitOveredMates.stream()
            .filter(mate -> mate.getGoal().getLimitOfSkippedDay() >= mate.getSkippedDayCount())
            .forEach(mate -> ReflectionTestUtils.setField(mate, "status", MateStatus.OUT));
    }

    @Override
    public Mate save(Mate mate) {
        ReflectionTestUtils.setField(mate, "id", mateId.getAndIncrement());
        map.put(mate.getId(), mate);
        return mate;
    }

    @Override
    public List<Mate> findYesterdaySkippedMates() {
        return map.values().stream()
            .filter(mate -> mate.getGoal().getCheckDays()
                .isDateCheckDayOfWeek(LocalDate.now().minusDays(1)))
            .filter(mate -> mate.getLastUploadDate() == null || mate.getLastUploadDate()
                .isBefore(LocalDate.now().minusDays(1)))
            .collect(Collectors.toList());
    }

    @Override
    public List<Mate> findAllWithGoal(List<Long> mateIds) {
        return mateIds.stream()
            .map(map::get)
            .toList();
    }

    @Override
    public OngoingGoalCount findOngoingCount(long userId) {
        int count = (int) map.values()
            .stream()
            .filter(mate -> mate.getUserId().equals(userId))
            .filter(mate -> mate.getStatus().equals(MateStatus.ONGOING))
            .count();
        return new OngoingGoalCount(count);
    }

    private int getOngoingCount(Mate m) {
        return (int) map.values()
            .stream()
            .filter(mt -> m.getUserId().equals(mt.getUserId()))
            .filter(mt -> mt.getStatus().equals(MateStatus.ONGOING))
            .count();
    }
}
