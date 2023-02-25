package checkmate.common.cache;

import checkmate.mate.domain.Mate;

import java.util.List;

public interface CacheHandler {
    void deleteMateCaches(List<Mate> mates);
}
