package checkmate.post.infrastructure;

import checkmate.post.domain.Image;
import checkmate.post.domain.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class ImageJpaRepository implements ImageRepository {
    private final EntityManager entityManager;

    @Override
    public void save(Image image) {
        entityManager.persist(image);
    }
}
