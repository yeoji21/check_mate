package checkmate.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKeyValueStorage implements KeyValueStorage {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteAll(long userId) {
        redisTemplate.delete(CacheKeyUtil.getKeyName(CacheKeyUtil.ONGOING_GOALS, userId));
        redisTemplate.delete(CacheKeyUtil.getKeyName(CacheKeyUtil.TODAY_GOALS, userId));
        redisTemplate.delete(CacheKeyUtil.getKeyName(CacheKeyUtil.HISTORY_GOALS, userId));
    }
}
