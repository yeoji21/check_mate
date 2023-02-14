package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.application.dto.response.TeamMateUploadInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalStatus;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        }

        //when
        List<TeamMateUploadInfo> uploadInfos = teamMateQueryDao.findTeamMateInfo(goal.getId());

        //then
        assertThat(uploadInfos.size()).isEqualTo(10);
        uploadInfos.forEach(info -> assertThat(info.getNickname()).isNotNull());
    }

    @Test
    void findSuccessTeamMates() throws Exception {
        //given
        User user = setSuccessedTeamMates();

        //when
        List<TeamMate> successTeamMates = teamMateQueryDao.findSuccessTeamMates(user.getId());

        //then
        assertThat(successTeamMates.size()).isEqualTo(2);
        successTeamMates.forEach(teamMate -> {
            assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.SUCCESS);
            assertThat(teamMate.getGoal().getStatus()).isEqualTo(GoalStatus.OVER);
        });
    }

    @Test
    void findTeamMateNicknames() throws Exception {
        //given
        List<Long> goalIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Goal goal = TestEntityFactory.goal(null, "goal" + i);
            em.persist(goal);
            goalIds.add(goal.getId());
            for (int j = 0; j < 10; j++) {
                User user = TestEntityFactory.user(null, i + "user" + j);
                em.persist(user);
                TeamMate teamMate = goal.join(user);
                ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
                em.persist(teamMate);
            }
        }
        em.flush();
        em.clear();

        //when
        Map<Long, List<String>> teamMateNicknames = teamMateQueryDao.findTeamMateNicknames(goalIds);

        //then
        assertThat(teamMateNicknames.keySet().size()).isEqualTo(3);
        teamMateNicknames.values()
                .forEach(nicknameList -> {
                    assertThat(nicknameList.size()).isEqualTo(10);
                    nicknameList.forEach(nickname -> assertThat(nickname).isNotNull());
                });
    }

    private User setSuccessedTeamMates() {
        User user = TestEntityFactory.user(null, "tester1");
        em.persist(user);

        Goal goal1 = TestEntityFactory.goal(null, "testGoal");
        ReflectionTestUtils.setField(goal1, "status", GoalStatus.OVER);
        em.persist(goal1);
        Goal goal2 = TestEntityFactory.goal(null, "testGoal");
        ReflectionTestUtils.setField(goal2, "status", GoalStatus.OVER);
        em.persist(goal2);

        TeamMate teamMate1 = goal1.join(user);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.SUCCESS);
        em.persist(teamMate1);

        TeamMate teamMate2 = goal2.join(user);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.SUCCESS);
        em.persist(teamMate2);

        em.flush();
        em.clear();

        return user;
    }
}