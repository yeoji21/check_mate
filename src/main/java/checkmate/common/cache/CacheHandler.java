package checkmate.common.cache;

import checkmate.goal.domain.TeamMate;

import java.util.List;

public interface CacheHandler {
    void deleteTeamMateCaches(List<TeamMate> teamMates);
}
