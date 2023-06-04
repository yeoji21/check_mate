package checkmate.common.cache;

import java.util.List;

public interface CacheHandler {

    void deleteUserCaches(List<Long> userIds);
}
