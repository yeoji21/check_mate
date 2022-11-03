package checkmate.common.cache;

import checkmate.goal.domain.TeamMate;

import java.util.List;

// TODO: 2022/10/04 개선 예정
public interface CacheTemplate {
    void deleteTMCacheData(List<TeamMate> TMs);
}
