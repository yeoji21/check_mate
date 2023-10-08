package checkmate.mate.domain;

import java.util.List;
import java.util.Optional;

public interface MateRepository {

    Optional<Mate> findById(long mateId);

    Optional<Mate> findWithGoal(long mateId);

    Optional<Mate> findWithGoal(long goalId, long userId);

    void increaseSkippedDayCount(List<Mate> mates);

    void updateLimitOveredMates(List<Mate> limitOveredMates);

    Mate save(Mate mate);

    List<Mate> findYesterdaySkippedMates();

    List<Mate> findAllWithGoal(List<Long> mateIds);

    OngoingGoalCount findOngoingCount(long userId);
}
