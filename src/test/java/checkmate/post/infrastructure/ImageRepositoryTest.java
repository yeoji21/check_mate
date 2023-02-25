package checkmate.post.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.post.domain.Image;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImageRepositoryTest extends RepositoryTest {
    @Test
    void findAllByUserId() throws Exception {
        //given
        User user1 = TestEntityFactory.user(null, "user1");
        em.persist(user1);
        User user2 = TestEntityFactory.user(null, "user2");
        em.persist(user2);

        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);

        Mate mate1 = goal.join(user1);
        em.persist(mate1);
        Mate mate2 = goal.join(user2);
        em.persist(mate2);

        Post post1 = TestEntityFactory.post(mate1);
        em.persist(post1);
        for (int i = 0; i < 3; i++) {
            Image image = Image.builder()
                    .post(post1)
                    .originalName("original" + i)
                    .storedName("stored" + i)
                    .build();
            em.persist(image);
        }

        Post post2 = TestEntityFactory.post(mate1);
        em.persist(post2);
        for (int i = 3; i < 6; i++) {
            Image image = Image.builder()
                    .post(post2)
                    .originalName("original" + i)
                    .storedName("stored" + i)
                    .build();
            em.persist(image);
        }

        Post post3 = TestEntityFactory.post(mate2);
        em.persist(post3);
        for (int i = 6; i < 9; i++) {
            Image image = Image.builder()
                    .post(post3)
                    .originalName("original" + i)
                    .storedName("stored" + i)
                    .build();
            em.persist(image);
        }

        em.flush();
        em.clear();

        //when
        List<Image> imageList = imageRepository.findAllByUserId(user1.getId());

        //then
        assertThat(imageList.size()).isEqualTo(6);
        imageList.forEach(image -> assertThat(image.getPost().getMate().getUserId()).isEqualTo(user1.getId()));
    }
}