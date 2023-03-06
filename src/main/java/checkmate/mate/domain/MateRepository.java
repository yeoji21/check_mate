package checkmate.mate.domain;

import java.util.List;
import java.util.Optional;

public interface MateRepository {
    Optional<Mate> findMateWithGoal(long mateId);

    Optional<Mate> findMateWithGoal(long goalId, long userId);

    List<Mate> updateYesterdaySkippedMates();

    void eliminateOveredMates(List<Mate> hookyTMs);

    List<Long> findMateUserIds(Long goalId);

    List<Mate> findMateInGoals(List<Long> goalIds);

    boolean isExistMate(long goalId, long userId);

    Mate save(Mate mate);
}
