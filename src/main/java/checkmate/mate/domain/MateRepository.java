package checkmate.mate.domain;

import java.util.List;
import java.util.Optional;

public interface MateRepository {
    Optional<Mate> findTeamMateWithGoal(long teamMateId);

    Optional<Mate> findTeamMateWithGoal(long goalId, long userId);

    List<Mate> updateYesterdayHookyTMs();

    List<Mate> eliminateOveredTMs(List<Mate> hookyTMs);

    List<Long> findTeamMateUserIds(Long goalId);

    List<Mate> findTeamMates(List<Long> goalIds);

    boolean isExistTeamMate(long goalId, long userId);

    Mate save(Mate mate);
}
