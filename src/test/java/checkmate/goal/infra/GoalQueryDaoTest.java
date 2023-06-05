package checkmate.goal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfo;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@AutoConfigureBefore(DataSourceAutoConfiguration.class)
class GoalQueryDaoTest extends RepositoryTest {

    @Test
    @DisplayName("어제가 종료일인 목표 목록 조회")
    void findYesterdayOveredGoals() throws Exception {
        //given
        Goal goal1 = createYesterDayOveredGoal();
        Goal goal2 = createYesterDayOveredGoal();
        Goal goal3 = createYesterDayOveredGoal();

        //when
        List<Long> overedGoalIds = goalQueryDao.findYesterdayOveredGoals();

        //then
        assertThat(overedGoalIds).hasSize(3);
        assertThat(overedGoalIds).contains(goal1.getId(), goal2.getId(), goal3.getId());
    }

    @Test
    void findCompleteNotificationDto() throws Exception {
        //given
        Goal goal1 = createGoal();
        Goal goal2 = createGoal();
        createMate(createUser("user1"), goal1);
        createMate(createUser("user2"), goal1);
        createMate(createUser("user3"), goal2);
        createMate(createUser("user4"), goal2);

        //when
        List<CompleteGoalNotificationDto> notificationDto =
            goalQueryDao.findCompleteNotificationDto(List.of(goal1.getId(), goal2.getId()));

        //then
        assertThat(notificationDto).hasSize(4);
        assertThat(notificationDto)
            .allMatch(dto -> dto.getGoalId() > 0L)
            .allMatch(dto -> dto.getUserId() > 0L)
            .allMatch(dto -> dto.getGoalTitle() != null);
    }

    @Test
    @DisplayName("유저의 진행 중인 목표들 간략 정보 조회")
    void findOngoingSimpleInfo() throws Exception {
        //given
        User user = createUser("user");
        createMate(user, createGoal());
        createMate(user, createGoal());
        createMate(user, createGoal());

        //when
        List<OngoingGoalInfo> ongoingGoals = goalQueryDao.findOngoingSimpleInfo(user.getId());

        //then
        assertThat(ongoingGoals).hasSize(3);
        assertThat(ongoingGoals)
            .allMatch(info -> info.getId() > 0L)
            .allMatch(info -> info.getTitle() != null)
            .allMatch(info -> info.getCategory() != null)
            .allMatch(info -> info.getWeekDays() != null);
    }

    @Test
    @DisplayName("목표 진행 스케쥴 조회")
    void findGoalScheduleInfo() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        GoalScheduleInfo goalScheduleInfo = goalQueryDao.findGoalScheduleInfo(goal.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(goalScheduleInfo.getStartDate()).isEqualTo(goal.getStartDate());
        assertThat(goalScheduleInfo.getEndDate()).isEqualTo(goal.getEndDate());
        assertThat(goalScheduleInfo.getSchedule()).isEqualTo(goal.getSchedule());
    }

    @Test
    @DisplayName("오늘 진행할 목표 정보")
    void findTodayGoalInfo() throws Exception {
        //given
        User user = createUser("user");
        createFutureStartGoal(user);
        createTodayStartGoal(user);

        //when
        List<TodayGoalInfo> todayGoals = goalQueryDao.findTodayGoalInfo(user.getId());

        //then
        assertThat(todayGoals).hasSize(1);
        assertThat(todayGoals).allMatch(goal -> CheckDaysConverter
            .isWorkingDay(new GoalCheckDays(goal.getCheckDays()).intValue(), LocalDate.now()));
    }

    @Test
    @DisplayName("목표 상세 정보 조회")
    void findDetailInfo() throws Exception {
        //given
        Goal goal = createGoal();
        createMate(createUser("user1"), goal);
        createMate(createUser("user2"), goal);
        createMate(createUser("user3"), goal);

        //when
        GoalDetailInfo info = goalQueryDao.findDetailInfo(goal.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getTitle()).isEqualTo(goal.getTitle());
        assertThat(info.getMates()).hasSize(3);
        assertThat(info.getMates())
            .allMatch(mate -> mate.getNickname() != null)
            .allMatch(mate -> !mate.isUploaded())
            .allMatch(mate -> mate.getMateId() > 0)
            .allMatch(mate -> mate.getUserId() > 0);
        assertThat(info.isInviteable()).isTrue();
    }

    private void createTodayStartGoal(User user) {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now());
        createMate(user, goal);
    }

    private void createFutureStartGoal(User user) {
        Goal goal = Goal.builder()
            .period(new GoalPeriod(LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)))
            .category(GoalCategory.ETC)
            .title("futureGoal")
            .checkDays(new GoalCheckDays("월화수목금토일"))
            .build();
        em.persist(goal);
        createMate(user, goal);
    }

    private User createUser(String name) {
        User user = TestEntityFactory.user(null, name);
        em.persist(user);
        return user;
    }

    private void createMate(User user, Goal goal) {
        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);
        return goal;
    }

    private Goal createYesterDayOveredGoal() {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "endDate", LocalDate.now().minusDays(1L));
        return goal;
    }
}