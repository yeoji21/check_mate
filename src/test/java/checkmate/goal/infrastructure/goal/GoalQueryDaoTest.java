package checkmate.goal.infrastructure.goal;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.common.util.WeekDayConverter;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static checkmate.goal.domain.QTeamMate.teamMate;
import static org.assertj.core.api.Assertions.assertThat;


class GoalQueryDaoTest extends RepositoryTest {
    @Test
    void findTeamMateInfo() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);
        User user1 = TestEntityFactory.user(null, "user1");
        em.persist(user1);
        goal.addTeamMate(TestEntityFactory.teamMate(null, user1.getId()));
        User user2 = TestEntityFactory.user(null, "user2");
        em.persist(user2);
        goal.addTeamMate(TestEntityFactory.teamMate(null, user2.getId()));
        User user3 = TestEntityFactory.user(null, "user3");
        em.persist(user3);
        goal.addTeamMate(TestEntityFactory.teamMate(null, user3.getId()));

        em.flush();
        em.clear();

        //when
        List<TeamMateUploadInfo> result = goalQueryDao.findTeamMateInfo(goal.getId());

        //then
        assertThat(result.size()).isEqualTo(3);
        for (int i = 0; i < result.size(); i++) {
            TeamMateUploadInfo info = result.get(i);
            assertThat(info.getNickname()).isEqualTo("user" + (i + 1));
            assertThat(info.isUploaded()).isFalse();
            assertThat(info.getId()).isNotNull();
            assertThat(info.getUserId()).isNotNull();
        }
    }

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
            assertThat(info.weekDays()).isNotBlank();
        }
    }

    @Test @DisplayName("목표 진행 스케쥴 조회")
    void findGoalPeriodInfo() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);

        //when
        GoalScheduleInfo goalScheduleInfo = goalQueryDao.findGoalPeriodInfo(goal.getId())
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
        em.persist(goal);

        em.createQuery("update Goal g set g.goalStatus = 'OVER' where g.id =:goalId")
                .setParameter("goalId", goal.getId())
                .executeUpdate();

        User tester1 = TestEntityFactory.user(null, "tester1");
        em.persist(tester1);
        User tester2 = TestEntityFactory.user(null, "tester2");
        em.persist(tester2);

        TeamMate teamMate1 = TestEntityFactory.teamMate(null, tester1.getId());
        goal.addTeamMate(teamMate1);

        TeamMate teamMate2 = TestEntityFactory.teamMate(null, tester2.getId());
        goal.addTeamMate(teamMate2);

        em.flush();
        em.clear();

        queryFactory.update(teamMate)
                .set(teamMate.teamMateStatus, TeamMateStatus.SUCCESS)
                .where(teamMate.id.in(teamMate1.getId(), teamMate2.getId()))
                .execute();

        //when
        List<GoalHistoryInfo> historyGoalList = goalQueryDao.findHistoryGoalInfo(tester1.getId());

        //then
        assertThat(historyGoalList.size()).isEqualTo(1);
        assertThat(historyGoalList.get(0).getTeamMateNames().size()).isEqualTo(goal.getTeam().size());
        assertThat(historyGoalList.get(0).getTeamMateNames().get(0)).isNotEqualTo(historyGoalList.get(0).getTeamMateNames().get(1));
    }

    @Test
    void findTodayGoalInfoDtoList() throws Exception{
        //given
        User tester = TestEntityFactory.user(null, "todayGoalFindTester");
        em.persist(tester);

        setFutureStartGoal(tester);
        setTodayStartGoal(tester);

        //when
        List<TodayGoalInfo> todayGoals = goalQueryDao.findTodayGoalInfo(tester.getId());
        todayGoals.forEach(g -> System.out.println(g.getTitle()));

        //then
        assertThat(todayGoals.size()).isEqualTo(1);
        assertThat(todayGoals.get(0).getWeekDays()).contains(WeekDayConverter.convertEngToKor(LocalDate.now()));
    }

    @Test
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

        //when
        GoalDetailInfo info = goalQueryDao.findDetailInfo(goal.getId(), user1.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getTitle()).isEqualTo(goal.getTitle());
        info.getTeamMates().forEach(tm -> assertThat(tm.getNickname()).isNotNull());
        assertThat(info.getTeamMates().size()).isEqualTo(3);
        assertThat(info.isInviteable()).isTrue();
    }

    private void setTodayStartGoal(User user) {
        Goal todayStart = Goal.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(20))
                .category(GoalCategory.기타)
                .title("todayGoal")
                .weekDays("월화수목금토일")
                .build();
        em.persist(todayStart);

        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        teamMate.changeToOngoingStatus(0);

        todayStart.addTeamMate(teamMate);
        em.persist(teamMate);
    }

    private void setFutureStartGoal(User user) {
        Goal futureGoal = Goal.builder()
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .category(GoalCategory.기타)
                .title("futureGoal")
                .weekDays("월화수목금토일")
                .build();
        em.persist(futureGoal);

        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        teamMate.changeToOngoingStatus(0);

        futureGoal.addTeamMate(teamMate);
        em.persist(teamMate);
    }
}