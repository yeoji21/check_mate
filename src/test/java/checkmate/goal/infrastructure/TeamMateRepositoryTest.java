package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static checkmate.goal.domain.QTeamMate.teamMate;
import static org.assertj.core.api.Assertions.assertThat;

class TeamMateRepositoryTest extends RepositoryTest {
    @Test
    void findByTeamMateId() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        TeamMate teamMate = goal.join(user);
        em.persist(teamMate);

        em.flush();
        em.clear();

        //when
        TeamMate findTeamMate = teamMateRepository.findTeamMateWithGoal(teamMate.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND, teamMate.getId()));

        //then
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

        User user1 = TestEntityFactory.user(null, "user1");
        em.persist(user1);
        TeamMate teamMate1 = goal1.join(user1);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate1);

        User user2 = TestEntityFactory.user(null, "user2");
        em.persist(user2);
        TeamMate teamMate2 = goal1.join(user2);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate2);

        User user3 = TestEntityFactory.user(null, "user3");
        em.persist(user3);
        TeamMate notUploadedTm = goal1.join(user3);
        ReflectionTestUtils.setField(notUploadedTm, "status", TeamMateStatus.ONGOING);
        em.persist(notUploadedTm);

        User user4 = TestEntityFactory.user(null, "user4");
        em.persist(user4);
        TeamMate notCheckedTm = goal1.join(user4);
        ReflectionTestUtils.setField(notCheckedTm, "status", TeamMateStatus.ONGOING);
        em.persist(notCheckedTm);

        Post post1 = Post.builder().teamMate(teamMate1).content("test").build();
        post1.check();
        em.persist(post1);

        Post post2 = Post.builder().teamMate(teamMate2).content("test").build();
        post2.check();
        em.persist(post2);

        Post post3 = Post.builder().teamMate(notCheckedTm).content("test").build();
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

        User user1 = TestEntityFactory.user(null, "user1");
        em.persist(user1);
        TeamMate teamMate1 = goal.join(user1);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate1);

        User user2 = TestEntityFactory.user(null, "user2");
        em.persist(user2);
        TeamMate teamMate2 = goal.join(user2);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate2);

        User user3 = TestEntityFactory.user(null, "user3");
        em.persist(user3);
        TeamMate teamMate3 = goal.join(user3);
        ReflectionTestUtils.setField(teamMate3, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate3);

        User user4 = TestEntityFactory.user(null, "user4");
        em.persist(user4);
        TeamMate teamMate4 = goal.join(user4);
        ReflectionTestUtils.setField(teamMate4, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate4);

        em.flush();
        em.clear();

        queryFactory.update(teamMate)
                .where(teamMate.id.in(teamMate1.getId(), teamMate2.getId()))
                .set(teamMate.progress.skippedDayCount, 50)
                .execute();

        //when
        List<TeamMate> eliminators = teamMateRepository.eliminateOveredTMs(queryFactory.selectFrom(teamMate).fetch());

        //then
        assertThat(eliminators.size()).isEqualTo(2);
        assertThat(eliminators).contains(teamMate1, teamMate2);
    }
}