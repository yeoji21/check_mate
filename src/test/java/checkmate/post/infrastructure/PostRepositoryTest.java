package checkmate.post.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
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
    void findByTeamMateIdsAndDate() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        TeamMate teamMate = goal.join(user);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate);

        Post post1 = Post.builder().teamMate(teamMate).content("post body text1").build();
        Post post2 = Post.builder().teamMate(teamMate).content("post body text2").build();
        Post post3 = Post.builder().teamMate(teamMate).content("post body text3").build();

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);

        em.flush();
        em.clear();

        //when
        Map<Post, List<Image>> postListMap = postRepository.findByTeamMateIdsAndDate(List.of(teamMate.getId()), LocalDate.now());

        //then
        assertThat(postListMap.size()).isEqualTo(3);

        for (Map.Entry<Post, List<Image>> postListEntry : postListMap.entrySet()) {
            assertThat(postListEntry.getKey().getTeamMate()).isEqualTo(teamMate);
        }
    }

}