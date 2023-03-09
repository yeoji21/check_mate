package checkmate.mate.infra;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MateQueryDaoTest extends RepositoryTest {
    @Test
    @DisplayName("팀원의 목표 캘린더 조회")
    void findMateCalendar() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal, "user");
        savePost(mate);
        em.flush();
        em.clear();

        //when
        MateScheduleInfo info = mateQueryDao.findMateCalendar(mate.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getStartDate()).isEqualTo(goal.getStartDate());
        assertThat(info.getGoalSchedule()).isEqualTo(goal.getSchedule());
        assertThat(info.getMateSchedule().length()).isEqualTo(info.getGoalSchedule().length());
    }

    @Test
    @DisplayName("팀원의 목표 인증 날짜 목록 조회")
    void findUploadedDates() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal, "user");
        createYesterdayUploadedPost(mate);
        savePost(mate);
        em.flush();
        em.clear();

        //when
        List<LocalDate> uploadedDates = mateQueryDao.findUploadedDates(mate.getId());

        //then
        assertThat(uploadedDates.size()).isEqualTo(2);
        assertThat(uploadedDates).contains(LocalDate.now().minusDays(1), LocalDate.now());
    }

    @Test
    @DisplayName("팀원의 인증 정보 조회")
    void findTeamMateInfo() throws Exception {
        //given
        Goal goal = createGoal();
        for (int i = 0; i < 10; i++) {
            createMate(goal, "user" + i);
        }
        em.flush();
        em.clear();

        //when
        List<MateUploadInfo> uploadInfo = mateQueryDao.findMateInfo(goal.getId());

        //then
        assertThat(uploadInfo.size()).isEqualTo(10);
        assertThat(uploadInfo).allMatch(info -> info.getNickname() != null);
    }

    @Test
    @DisplayName("목표 수행 성공한 팀원 목록 조회")
    void findSuccessTeamMates() throws Exception {
        //given
        User user = createSuccessedMates();
        em.flush();
        em.clear();

        //when
        List<Mate> successMates = mateQueryDao.findSuccessMates(user.getId());

        //then
        assertThat(successMates.size()).isEqualTo(2);
        assertThat(successMates).allMatch(mate ->
                mate.getStatus() == MateStatus.SUCCESS &&
                        mate.getGoal().getStatus() == GoalStatus.OVER);
    }

    @Test
    @DisplayName("팀원들의 닉네임 조회")
    void findTeamMateNicknames() throws Exception {
        //given
        List<Long> goalIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Goal goal = createGoal();
            goalIds.add(goal.getId());
            for (int j = 0; j < 10; j++) createMate(goal, i + "user" + j);
        }
        em.flush();
        em.clear();

        //when
        Map<Long, List<String>> mateNicknames = mateQueryDao.findMateNicknames(goalIds);

        //then
        assertThat(mateNicknames.keySet().size()).isEqualTo(3);
        mateNicknames.values().forEach(nicknames -> {
            assertThat(nicknames.size()).isEqualTo(10);
            assertThat(nicknames).allMatch(nickname -> nickname != null);
        });
    }

    private User createSuccessedMates() {
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

    private void savePost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        em.persist(post);
    }

    private void createYesterdayUploadedPost(Mate mate) {
        Post yesterDayPost = TestEntityFactory.post(mate);
        ReflectionTestUtils.setField(yesterDayPost, "uploadedDate", LocalDate.now().minusDays(1));
        em.persist(yesterDayPost);
    }

    private Mate createMate(Goal goal, String nickname) {
        User user = TestEntityFactory.user(null, nickname);
        em.persist(user);
        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);
        return goal;
    }
}