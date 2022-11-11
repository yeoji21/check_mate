package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.TeamMateNotFoundException;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static checkmate.goal.domain.QTeamMate.teamMate;
import static org.assertj.core.api.Assertions.assertThat;

class TeamMateRepositoryTest extends RepositoryTest {

    @Test
    void findByTeamMateId() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        em.persist(goal);

        goal.addTeamMate(teamMate);
        em.persist(teamMate);

        em.flush();
        em.clear();

        //when
        TeamMate findTeamMate = teamMateRepository.findTeamMate(teamMate.getId()).orElseThrow(TeamMateNotFoundException::new);

        //then
        assertThat(findTeamMate.getStatus()).isEqualTo(TeamMateStatus.ONGOING);
        assertThat(findTeamMate.getGoal().getId()).isEqualTo(goal.getId());
        assertThat(findTeamMate.getId()).isEqualTo(teamMate.getId());
    }

    @Test
    void updateYesterdayHookyTMs() throws Exception{
        //given
        Goal goal1 = TestEntityFactory.goal(null, "goal1");
        em.persist(goal1);
        Goal goal2 = TestEntityFactory.goal(null, "goal2");
        em.persist(goal2);

        TeamMate teamMate1 = TestEntityFactory.teamMate(null, 1L);
        goal1.addTeamMate(teamMate1);
        em.persist(teamMate1);

        TeamMate teamMate2 = TestEntityFactory.teamMate(null, 2L);
        goal2.addTeamMate(teamMate2);
        em.persist(teamMate2);

        TeamMate notUploadedTm = TestEntityFactory.teamMate(null, 3L);
        goal1.addTeamMate(notUploadedTm);
        em.persist(notUploadedTm);

        TeamMate notCheckedTm = TestEntityFactory.teamMate(null, 4L);
        goal2.addTeamMate(notCheckedTm);
        em.persist(notCheckedTm);

        Post post1 = Post.builder().teamMate(teamMate1).text("test").build();
        post1.check();
        em.persist(post1);

        Post post2 = Post.builder().teamMate(teamMate2).text("test").build();
        post2.check();
        em.persist(post2);

        Post post3 = Post.builder().teamMate(notCheckedTm).text("test").build();
        em.persist(post3);

        em.flush();
        em.clear();

        //when
        List<TeamMate> hookyTeamMates = teamMateRepository.updateYesterdayHookyTMs();

        //then
        assertThat(hookyTeamMates.size()).isEqualTo(2);
        assertThat(hookyTeamMates).contains(notUploadedTm, notCheckedTm);
    }

    @Test
    void eliminateOveredTMs() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "goal1");
        em.persist(goal);

        TeamMate teamMate1 = TestEntityFactory.teamMate(null, 1L);
        goal.addTeamMate(teamMate1);
        em.persist(teamMate1);

        TeamMate teamMate2 = TestEntityFactory.teamMate(null, 2L);
        goal.addTeamMate(teamMate2);
        em.persist(teamMate2);

        TeamMate teamMate3 = TestEntityFactory.teamMate(null, 3L);
        goal.addTeamMate(teamMate3);
        em.persist(teamMate3);

        TeamMate teamMate4 = TestEntityFactory.teamMate(null, 4L);
        goal.addTeamMate(teamMate4);
        em.persist(teamMate4);

        em.flush();
        em.clear();

        queryFactory.update(teamMate)
                .where(teamMate.id.in(teamMate1.getId(), teamMate2.getId()))
                .set(teamMate.progress.hookyDays, 50)
                .execute();

        //when
        List<TeamMate> eliminators = teamMateRepository.eliminateOveredTMs(queryFactory.selectFrom(teamMate).fetch());

        //then
        assertThat(eliminators.size()).isEqualTo(2);
        assertThat(eliminators).contains(teamMate1, teamMate2);
    }
}