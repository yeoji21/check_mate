package checkmate.common.cache;

import java.util.List;

// TODO: 2023/06/23 KeyValueStore 등으로 변경
public interface CacheHandler {

    void deleteUserCaches(List<Long> userIds);
}
