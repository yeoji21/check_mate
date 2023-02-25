package checkmate.post.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.post.domain.Image;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends RepositoryTest {
    @Test
    void findByTeamMateIdsAndDate() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);

        Post post1 = TestEntityFactory.post(mate);
        Post post2 = TestEntityFactory.post(mate);
        Post post3 = TestEntityFactory.post(mate);

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);

        em.flush();
        em.clear();

        //when
        Map<Post, List<Image>> postListMap = postRepository.findByMateIdsAndDate(List.of(mate.getId()), LocalDate.now());

        //then
        assertThat(postListMap.size()).isEqualTo(3);

        for (Map.Entry<Post, List<Image>> postListEntry : postListMap.entrySet()) {
            assertThat(postListEntry.getKey().getMate()).isEqualTo(mate);
        }
    }

}