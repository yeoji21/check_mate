package checkmate.post.infrastructure;

import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static checkmate.post.domain.QLikes.likes;
import static checkmate.post.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostJpaRepository implements PostRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<Post> findById(long postId) {
        return Optional.ofNullable(entityManager.find(Post.class, postId));
    }

    @Override
    public Optional<Post> findWithLikes(long postId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(post)
                        .leftJoin(post.likes, likes).fetchJoin()
                        .where(post.id.eq(postId))
                        .fetchOne()
        );
    }

    @Override
    public void save(Post post) {
        entityManager.persist(post);
    }
}
