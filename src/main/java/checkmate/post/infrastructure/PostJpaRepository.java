package checkmate.post.infrastructure;

import checkmate.post.domain.Image;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.post.domain.QImage.image;
import static checkmate.post.domain.QPost.post;
import static com.querydsl.core.group.GroupBy.list;

@Repository
@RequiredArgsConstructor
public class PostJpaRepository implements PostRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Map<Post, List<Image>> findByTeamMateIdsAndDate(List<Long> teamMateIds, LocalDate uploadDate) {
        return queryFactory
                .from(post)
                .leftJoin(post.images.images, image)
                .join(post.teamMate, teamMate).fetchJoin()
                .where(post.teamMate.id.in(teamMateIds),
                        post.createdDateTime.between(uploadDate.atStartOfDay(), uploadDate.plusDays(1).atStartOfDay()))
                .transform(GroupBy.groupBy(post).as(list(image)));
    }

    @Override
    public Optional<Post> findById(long postId) {
        return Optional.ofNullable(queryFactory.selectFrom(post)
                .join(post.teamMate, teamMate).fetchJoin()
                .join(teamMate.goal, goal).fetchJoin()
                .where(post.id.eq(postId))
                .fetchOne());
    }

    @Override
    public void save(Post post) {
        entityManager.persist(post);
    }
}
