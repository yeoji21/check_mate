package checkmate.common.cache;

import checkmate.config.redis.RedisKey;
import checkmate.goal.domain.TeamMate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisCacheTemplate implements CacheTemplate{
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteTMCacheData(List<TeamMate> TMs) {
        List<Long> eliminatorUserIds = TMs.stream()
                .map(TeamMate::getUserId)
                .collect(Collectors.toList());

        redisTemplate.delete(RedisKey.getRedisKeyList(RedisKey.ONGOING_GOALS, eliminatorUserIds));
        redisTemplate.delete(RedisKey.getRedisKeyList(RedisKey.TODAY_GOALS, eliminatorUserIds));
        redisTemplate.delete(RedisKey.getHistoryKeyList(eliminatorUserIds));
    }
}
