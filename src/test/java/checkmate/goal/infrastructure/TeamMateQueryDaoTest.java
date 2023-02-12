package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.application.dto.response.TeamMateUploadInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMateQueryDaoTest extends RepositoryTest {

    @Test
    void 팀원의_목표캘린더_조회() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        TeamMate teamMate = goal.join(user);
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

    @Test
    void findUploadedDates() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        TeamMate teamMate = goal.join(user);
        em.persist(teamMate);

        Post yesterDayPost = TestEntityFactory.post(teamMate);
        ReflectionTestUtils.setField(yesterDayPost, "uploadedDate", LocalDate.now().minusDays(1));
        em.persist(yesterDayPost);
        Post todayPost = TestEntityFactory.post(teamMate);
        em.persist(todayPost);

        //when
        List<LocalDate> uploadedDates = teamMateQueryDao.findUploadedDates(teamMate.getId());

        //then
        assertThat(uploadedDates.size()).isEqualTo(2);
        assertThat(uploadedDates.get(0)).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(uploadedDates.get(1)).isEqualTo(LocalDate.now());
    }

    @Test
    void findTeamMateInfo() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        for (int i = 0; i < 10; i++) {
            User user = TestEntityFactory.user(null, "user" + i);
            em.persist(user);
            TeamMate teamMate = goal.join(user);
            em.persist(teamMate);
            teamMate.initiateGoal(0);
        }

        //when
        List<TeamMateUploadInfo> uploadInfos = teamMateQueryDao.findTeamMateInfo(goal.getId());

        //then
        assertThat(uploadInfos.size()).isEqualTo(10);
        uploadInfos.forEach(info -> assertThat(info.getNickname()).isNotNull());
    }
}