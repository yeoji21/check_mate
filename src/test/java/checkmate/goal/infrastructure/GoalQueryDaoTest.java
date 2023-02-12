package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.*;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureBefore(DataSourceAutoConfiguration.class)
class GoalQueryDaoTest extends RepositoryTest {
    @Test
    @DisplayName("유저의 진행 중인 목표들 간략 정보 조회")
    void findOngoingSimpleInfo() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);

        Goal goal1 = TestEntityFactory.goal(null, "goal1");
        Goal goal2 = TestEntityFactory.goal(null, "goal2");
        Goal goal3 = TestEntityFactory.goal(null, "goal3");

        em.persist(goal1);
        em.persist(goal2);
        em.persist(goal3);

        TeamMate teamMate1 = goal1.join(user);
        TeamMate teamMate2 = goal2.join(user);
        TeamMate teamMate3 = goal3.join(user);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        ReflectionTestUtils.setField(teamMate3, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate1);
        em.persist(teamMate2);
        em.persist(teamMate3);

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

    @Test
    @DisplayName("목표 진행 스케쥴 조회")
    void findGoalScheduleInfo() throws Exception {
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
    void 성공한_목표_목록_조회() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        ReflectionTestUtils.setField(goal, "status", GoalStatus.OVER);
        em.persist(goal);

        User tester1 = TestEntityFactory.user(null, "tester1");
        em.persist(tester1);
        User tester2 = TestEntityFactory.user(null, "tester2");
        em.persist(tester2);

        TeamMate teamMate1 = goal.join(tester1);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.SUCCESS);
        em.persist(teamMate1);

        TeamMate teamMate2 = goal.join(tester2);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.SUCCESS);
        em.persist(teamMate2);

        em.flush();
        em.clear();

        //when
        List<GoalHistoryInfo> historyGoalList = goalQueryDao.findHistoryGoalInfo(tester1.getId());

        //then
        assertThat(historyGoalList.size()).isEqualTo(1);
        assertThat(historyGoalList.get(0).getTeamMateNames().size()).isEqualTo(2);
        assertThat(historyGoalList.get(0).getTeamMateNames().get(0)).isNotEqualTo(historyGoalList.get(0).getTeamMateNames().get(1));
    }

    @Test
    @DisplayName("오늘 진행할 목표 정보")
    void findTodayGoalInfo() throws Exception {
        //given
        User tester = TestEntityFactory.user(null, "todayGoalFindTester");
        em.persist(tester);

        setFutureStartGoal(tester);
        setTodayStartGoal(tester);

        //when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<TodayGoalInfo> todayGoals = goalQueryDao.findTodayGoalInfo(tester.getId());
        stopWatch.stop();
        System.out.println("time : " + stopWatch.getTotalTimeMillis());
        System.out.println(todayGoals.get(0).getCheckDays());

        //then
        assertThat(todayGoals.size()).isEqualTo(1);
        todayGoals.forEach(
                goal -> assertThat(CheckDaysConverter.isWorkingDay(new GoalCheckDays(goal.getCheckDays()).intValue(), LocalDate.now()))
                        .isTrue()
        );
    }

    @Test
    @DisplayName("목표 상세 정보 조회")
    void findDetailInfo() throws Exception {
        //given
        User user1 = TestEntityFactory.user(null, "tester1");
        User user2 = TestEntityFactory.user(null, "tester2");
        User user3 = TestEntityFactory.user(null, "tester3");
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);

        TeamMate teamMate1 = goal.join(user1);
        TeamMate teamMate2 = goal.join(user2);
        TeamMate teamMate3 = goal.join(user3);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        ReflectionTestUtils.setField(teamMate3, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate1);
        em.persist(teamMate2);
        em.persist(teamMate3);

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
            assertThat(tm.getTeamMateId()).isNotNull();
            assertThat(tm.getUserId()).isNotNull();
        }
        assertThat(info.getTeamMates().size()).isEqualTo(3);
        assertThat(info.isInviteable()).isTrue();
    }

    private void setTodayStartGoal(User user) {
        Goal todayStartGoal = Goal.builder()
                .period(new GoalPeriod(LocalDate.now(), LocalDate.now().plusDays(20)))
                .category(GoalCategory.ETC)
                .title("todayGoal")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .build();
        em.persist(todayStartGoal);

        TeamMate teamMate = todayStartGoal.join(user);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate);
    }

    private void setFutureStartGoal(User user) {
        Goal futureGoal = Goal.builder()
                .period(new GoalPeriod(LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)))
                .category(GoalCategory.ETC)
                .title("futureGoal")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .build();
        em.persist(futureGoal);

        TeamMate teamMate = futureGoal.join(user);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate);
    }
}