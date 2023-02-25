package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalStatus;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MateQueryDaoTest extends RepositoryTest {

    @Test
    void 팀원의_목표캘린더_조회() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        Mate mate = goal.join(user);
        em.persist(mate);
        Post post = TestEntityFactory.post(mate);
        em.persist(post);

        //when
        MateScheduleInfo info = teamMateQueryDao.getTeamMateCalendar(mate.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getStartDate()).isEqualTo(goal.getStartDate());
        assertThat(info.getGoalSchedule()).isEqualTo(goal.getSchedule());
        assertThat(info.getMateSchedule().length()).isEqualTo(info.getGoalSchedule().length());
    }

    @Test
    void findUploadedDates() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        Mate mate = goal.join(user);
        em.persist(mate);

        Post yesterDayPost = TestEntityFactory.post(mate);
        ReflectionTestUtils.setField(yesterDayPost, "uploadedDate", LocalDate.now().minusDays(1));
        em.persist(yesterDayPost);
        Post todayPost = TestEntityFactory.post(mate);
        em.persist(todayPost);

        //when
        List<LocalDate> uploadedDates = teamMateQueryDao.findUploadedDates(mate.getId());

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
            Mate mate = goal.join(user);
            em.persist(mate);
            ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        }

        //when
        List<MateUploadInfo> uploadInfos = teamMateQueryDao.findTeamMateInfo(goal.getId());

        //then
        assertThat(uploadInfos.size()).isEqualTo(10);
        uploadInfos.forEach(info -> assertThat(info.getNickname()).isNotNull());
    }

    @Test
    void findSuccessTeamMates() throws Exception {
        //given
        User user = setSuccessedTeamMates();

        //when
        List<Mate> successMates = teamMateQueryDao.findSuccessTeamMates(user.getId());

        //then
        assertThat(successMates.size()).isEqualTo(2);
        successMates.forEach(teamMate -> {
            assertThat(teamMate.getStatus()).isEqualTo(MateStatus.SUCCESS);
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
                Mate mate = goal.join(user);
                ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
                em.persist(mate);
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

        Mate mate1 = goal1.join(user);
        ReflectionTestUtils.setField(mate1, "status", MateStatus.SUCCESS);
        em.persist(mate1);

        Mate mate2 = goal2.join(user);
        ReflectionTestUtils.setField(mate2, "status", MateStatus.SUCCESS);
        em.persist(mate2);

        em.flush();
        em.clear();

        return user;
    }
}