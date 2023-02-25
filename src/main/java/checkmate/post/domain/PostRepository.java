package checkmate.post.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PostRepository {
    Map<Post, List<Image>> findByMateIdsAndDate(List<Long> mateIds, LocalDate uploadDate);

    Optional<Post> findById(long postId);

    void save(Post post);
}
