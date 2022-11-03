package checkmate.config.redis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisKeyGeneratorTest {
    @Test
    void getRedisKeyList() throws Exception{
        //given
        List<Long> userIdList = List.of(11L, 12L, 11034L);

        //when
        List<String> redisKeyList = RedisKey.getRedisKeyList(RedisKey.ONGOING_GOALS, userIdList);

        //then
        assertThat(redisKeyList.size()).isEqualTo(3);
        assertThat(redisKeyList.get(0)).startsWith(RedisKey.ONGOING_GOALS);
    }
}