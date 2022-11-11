package checkmate.goal.infrastructure.goal;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.common.util.WeekDayConverter;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.*;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class GoalQueryDaoTest extends RepositoryTest {
    @Test @DisplayName("유저의 진행 중인 목표들 간략 정보 조회")
    void findOngoingSimpleInfo() throws Exception{
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);

        Goal goal1 = TestEntityFactory.goal(null, "goal1");
        Goal goal2 = TestEntityFactory.goal(null, "goal2");
        Goal goal3 = TestEntityFactory.goal(null, "goal3");

        em.persist(goal1);
        em.persist(goal2);
        em.persist(goal3);

        goal1.addTeamMate(TestEntityFactory.teamMate(null, user.getId()));
        goal2.addTeamMate(TestEntityFactory.teamMate(null, user.getId()));
        goal3.addTeamMate(TestEntityFactory.teamMate(null, user.getId()));

        //when
        List<GoalSimpleInfo> ongoingGoals = goalQueryDao.findOngoingSimpleInfo(user.getId());

        //then
        assertThat(ongoingGoals.size()).isEqualTo(3);
        for (int i = 0; i < ongoingGoals.size(); i++) {
            GoalSimpleInfo info = ongoingGoals.get(i);
            assertThat(info.id()).isGreaterThan(0L);
            assertThat(info.title()).isEqualTo("goal" + (i + 1));
            assertThat(info.category()).isNotNull();
            assertThat(info.weekDays()).isEqualTo("월화수목금토일");
        }
    }

    @Test @DisplayName("목표 진행 스케쥴 조회")
    void findGoalScheduleInfo() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);

        //when
        GoalScheduleInfo goalScheduleInfo = goalQueryDao.findGoalScheduleInfo(goal.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(goalScheduleInfo.getStartDate()).isEqualTo(goal.getStartDate());
        assertThat(goalScheduleInfo.getEndDate()).isEqualTo(goal.getEndDate());
        assertThat(goalScheduleInfo.getSchedule()).isEqualTo(goal.getSchedule());
    }

    @Test
    void 성공한_목표_목록_조회() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        ReflectionTestUtils.setField(goal, "goalStatus", GoalStatus.OVER);
        em.persist(goal);

        User tester1 = TestEntityFactory.user(null, "tester1");
        em.persist(tester1);
        User tester2 = TestEntityFactory.user(null, "tester2");
        em.persist(tester2);

        TeamMate teamMate1 = TestEntityFactory.teamMate(null, tester1.getId());
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.SUCCESS);
        goal.addTeamMate(teamMate1);

        TeamMate teamMate2 = TestEntityFactory.teamMate(null, tester2.getId());
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.SUCCESS);
        goal.addTeamMate(teamMate2);

        em.flush();
        em.clear();

        //when
        List<GoalHistoryInfo> historyGoalList = goalQueryDao.findHistoryGoalInfo(tester1.getId());

        //then
        assertThat(historyGoalList.size()).isEqualTo(1);
        assertThat(historyGoalList.get(0).getTeamMateNames().size()).isEqualTo(2);
        assertThat(historyGoalList.get(0).getTeamMateNames().get(0)).isNotEqualTo(historyGoalList.get(0).getTeamMateNames().get(1));
    }

    @Test @DisplayName("오늘 진행할 목표 정보")
    void findTodayGoalInfo() throws Exception{
        //given
        User tester = TestEntityFactory.user(null, "todayGoalFindTester");
        em.persist(tester);

        setFutureStartGoal(tester);
        setTodayStartGoal(tester);

        //when
        List<TodayGoalInfo> todayGoals = goalQueryDao.findTodayGoalInfo(tester.getId());

        //then
        assertThat(todayGoals.size()).isEqualTo(1);
        assertThat(todayGoals.get(0).getCheckDays()).contains(WeekDayConverter.convertEngToKor(LocalDate.now()));
    }

    @Test @DisplayName("목표 상세 정보 조회")
    void findDetailInfo() throws Exception{
        //given
        User user1 = TestEntityFactory.user(null, "tester1");
        User user2 = TestEntityFactory.user(null, "tester2");
        User user3 = TestEntityFactory.user(null, "tester3");
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);

        TeamMate teamMate1 = TestEntityFactory.teamMate(null, user1.getId());
        TeamMate teamMate2 = TestEntityFactory.teamMate(null, user2.getId());
        TeamMate teamMate3 = TestEntityFactory.teamMate(null, user3.getId());
        goal.addTeamMate(teamMate1);
        goal.addTeamMate(teamMate2);
        goal.addTeamMate(teamMate3);

        em.flush();
        em.clear();

        //when
        GoalDetailInfo info = goalQueryDao.findDetailInfo(goal.getId(), user1.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getTitle()).isEqualTo(goal.getTitle());
        for (int i = 0; i < info.getTeamMates().size(); i++) {
            TeamMateUploadInfo tm = info.getTeamMates().get(i);
            assertThat(tm.getNickname()).isEqualTo("tester" + (i + 1));
            assertThat(tm.isUploaded()).isFalse();
            assertThat(tm.getId()).isNotNull();
            assertThat(tm.getUserId()).isNotNull();
        }
        assertThat(info.getTeamMates().size()).isEqualTo(3);
        assertThat(info.isInviteable()).isTrue();
    }

    private void setTodayStartGoal(User user) {
        Goal todayStart = Goal.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(20))
                .category(GoalCategory.ETC)
                .title("todayGoal")
                .checkDays("월화수목금토일")
                .build();
        em.persist(todayStart);

        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        todayStart.addTeamMate(teamMate);
        teamMate.initiateGoal(0);
        em.persist(teamMate);
    }

    private void setFutureStartGoal(User user) {
        Goal futureGoal = Goal.builder()
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .category(GoalCategory.ETC)
                .title("futureGoal")
                .checkDays("월화수목금토일")
                .build();
        em.persist(futureGoal);

        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        futureGoal.addTeamMate(teamMate);
        teamMate.initiateGoal(0);
        em.persist(teamMate);
    }
}