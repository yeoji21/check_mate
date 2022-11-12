package checkmate.goal.domain;

import java.util.List;
import java.util.Optional;

public interface TeamMateRepository {
    Optional<TeamMate> findTeamMateWithGoal(long teamMateId);
    Optional<TeamMate> findTeamMateWithGoal(long goalId, long userId);
    List<TeamMate> updateYesterdayHookyTMs();
    List<TeamMate> eliminateOveredTMs(List<TeamMate> hookyTMs);
    List<Long> findTeamMateUserIds(Long goalId);
    List<TeamMate> findTeamMates(List<Long> goalIds);
    void save(TeamMate teamMate);
}
