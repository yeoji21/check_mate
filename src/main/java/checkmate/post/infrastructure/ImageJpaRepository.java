package checkmate.post.infrastructure;

import checkmate.post.domain.Image;
import checkmate.post.domain.ImageRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.post.domain.QImage.image;
import static checkmate.post.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class ImageJpaRepository implements ImageRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public void save(Image image) {
        entityManager.persist(image);
    }

    @Override
    public List<Image> findAllByUserId(long userId) {
        return queryFactory.selectFrom(image)
                .join(image.post, post)
                .join(post.teamMate, teamMate)
                .where(post.teamMate.userId.eq(userId))
                .fetch();
    }
}
