package checkmate.goal.infrastructure.mate;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMateQueryDaoTest extends RepositoryTest {

    @Test
    void 팀원의_목표캘린더_조회() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);

        TeamMate teamMate = TestEntityFactory.teamMate(null, 1L);
        goal.addTeamMate(teamMate);
        em.persist(teamMate);

        Post post = TestEntityFactory.post(teamMate);
        em.persist(post);

        //when
        TeamMateScheduleInfo info = teamMateQueryDao.getTeamMateCalendar(teamMate.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getStartDate()).isEqualTo(goal.getStartDate());
        assertThat(info.getGoalSchedule()).isEqualTo(goal.getSchedule());
        assertThat(info.getTeamMateSchedule().length()).isEqualTo(info.getGoalSchedule().length());
    }
}