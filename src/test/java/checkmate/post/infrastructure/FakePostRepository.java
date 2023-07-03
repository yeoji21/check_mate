package checkmate.post.infrastructure;

import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakePostRepository implements PostRepository {

    private final AtomicLong postId = new AtomicLong(1);
    private final Map<Long, Post> map = new HashMap<>();

    @Override
    public Optional<Post> findById(long postId) {
        return Optional.ofNullable(map.get(postId));
    }

    @Override
    public Optional<Post> findWithLikes(long postId) {
        return Optional.ofNullable(map.get(postId));
    }

    @Override
    public void save(Post post) {
        ReflectionTestUtils.setField(post, "id", postId.getAndIncrement());
        map.put(post.getId(), post);
    }
}
