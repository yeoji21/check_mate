package checkmate.common.cache;

import java.util.List;

// TODO: 2023/06/23 UserRepository 내부로 ?
public interface CacheHandler {

    void deleteUserCaches(List<Long> userIds);
}
