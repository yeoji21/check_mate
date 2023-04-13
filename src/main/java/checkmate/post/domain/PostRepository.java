package checkmate.post.domain;

import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(long postId);

    Optional<Post> findWithLikes(long postId);

    void save(Post post);
}
