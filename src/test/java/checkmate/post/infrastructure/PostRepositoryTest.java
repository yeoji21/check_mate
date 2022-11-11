package checkmate.post.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.post.domain.Image;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends RepositoryTest {
    private Goal goal;
    private TeamMate teamMate;
    private Post post1, post2, post3;

    @BeforeEach
    void setUp() {
        goal = TestEntityFactory.goal(null, "testGoal");
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        teamMate = TestEntityFactory.teamMate(null, user.getId());
        goal.addTeamMate(teamMate);
        teamMate.initiateGoal(0);

        em.persist(goal);
        em.persist(teamMate);

        post1 = Post.builder().teamMate(teamMate).text("post body text1").build();
        post2 = Post.builder().teamMate(teamMate).text("post body text2").build();
        post3 = Post.builder().teamMate(teamMate).text("post body text3").build();

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);

        em.flush();
        em.clear();
    }

    @Test
    void findByTeamMateIdsAndDate() throws Exception{
        //given

        //when
        Map<Post, List<Image>> postListMap = postRepository.findByTeamMateIdsAndDate(List.of(teamMate.getId()), LocalDate.now());

        //then
        assertThat(postListMap.size()).isEqualTo(3);

        for (Map.Entry<Post, List<Image>> postListEntry : postListMap.entrySet()) {
            assertThat(postListEntry.getKey().getTeamMate()).isEqualTo(teamMate);
        }
    }

}