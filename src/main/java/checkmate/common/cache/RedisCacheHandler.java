package checkmate.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisCacheHandler implements CacheHandler {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteUserCaches(List<Long> userIds) {
        redisTemplate.delete(CacheKey.ongoingGoalsKeys(userIds));
        redisTemplate.delete(CacheKey.todayGoalsKeys(userIds));
        redisTemplate.delete(CacheKey.historyGoalsKeys(userIds));
    }
}
