package checkmate.common.cache;

import checkmate.goal.domain.TeamMate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisCacheHandler implements CacheHandler {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteTeamMateCaches(List<TeamMate> teamMates) {
        List<Long> eliminatorUserIds = teamMates.stream()
                .map(TeamMate::getUserId)
                .collect(Collectors.toList());

        redisTemplate.delete(CacheKey.ongoingGoalsKeys(eliminatorUserIds));
        redisTemplate.delete(CacheKey.todayGoalsKeys(eliminatorUserIds));
        redisTemplate.delete(CacheKey.historyGoalsKeys(eliminatorUserIds));
    }
}
