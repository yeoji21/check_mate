package checkmate.goal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfo;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalCheckDays.CheckDaysConverter;
import checkmate.goal.domain.GoalPeriod;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.user.domain.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@AutoConfigureBefore(DataSourceAutoConfiguration.class)
class GoalQueryDaoTest extends RepositoryTest {

    @Test
    void findOngoingUserIds() throws Exception {
        //given
        Goal goal1 = createGoal();
        Goal goal2 = createGoal();
        createOngoingMate(goal1);
        createOngoingMate(goal1);
        createOngoingMate(goal2);
        createOutMate(goal2);

        //when
        List<Long> userIds = goalQueryDao.findOngoingUserIds(
            List.of(goal1.getId(), goal2.getId()));

        //then
        assertThat(userIds).hasSize(3);
    }

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
        assertThat(overedGoalIds).contains(goal1.getId(), goal2.getId(), goal3.getId());
    }

    @Test
    void findCompleteNotificationDto() throws Exception {
        //given
        Goal goal1 = createGoal();
        Goal goal2 = createGoal();
        createOngoingMate(goal1);
        createOngoingMate(goal1);
        createOngoingMate(goal2);
        createOngoingMate(goal2);

        //when
        List<CompleteGoalNotificationDto> notificationDto =
            goalQueryDao.findCompleteNotificationDto(List.of(goal1.getId(), goal2.getId()));

        //then
        assertThat(notificationDto)
            .hasSize(4)
            .allMatch(dto -> dto.getGoalId() > 0L)
            .allMatch(dto -> dto.getUserId() > 0L)
            .allMatch(dto -> dto.getGoalTitle() != null);
    }

    @Test
    @DisplayName("유저의 진행 중인 목표들 간략 정보 조회")
    void findOngoingSimpleInfo() throws Exception {
        //given
        User user = createUser();
        createOngoingMate(createGoal(), user);
        createOngoingMate(createGoal(), user);
        createOngoingMate(createGoal(), user);

        //when
        List<OngoingGoalInfo> ongoingGoals = goalQueryDao.findOngoingGoalInfo(user.getId());

        //then
        assertThat(ongoingGoals)
            .hasSize(3)
            .allMatch(info -> info.getGoalId() > 0L)
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
    }

    @Test
    @DisplayName("오늘 진행할 목표 정보")
    void findTodayGoalInfo() throws Exception {
        //given
        User user = createUser();
        createOngoingMate(createFutureStartGoal(), user);
        createOngoingMate(createTodayStartGoal(), user);

        //when
        List<TodayGoalInfo> todayGoals = goalQueryDao.findTodayGoalInfo(user.getId());

        //then
        assertThat(todayGoals)
            .hasSize(1)
            .allMatch(this::isToday);
    }

    @Test
    @DisplayName("목표 상세 정보 조회")
    void findDetailInfo() throws Exception {
        //given
        Goal goal = createGoal();
        createOngoingMate(goal);
        createOngoingMate(goal);
        createOngoingMate(goal);

        //when
        GoalDetailInfo info = goalQueryDao.findDetailInfo(goal.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getTitle()).isEqualTo(goal.getTitle());
        assertThat(info.isInviteable()).isTrue();
        assertThat(info.getMates())
            .hasSize(3)
            .allMatch(mate -> mate.getNickname() != null)
            .allMatch(mate -> !mate.isUploaded())
            .allMatch(mate -> mate.getMateId() > 0)
            .allMatch(mate -> mate.getUserId() > 0);
    }

    private boolean isToday(TodayGoalInfo goal) {
        String korWeekDays = goal.getCheckDays();
        return GoalCheckDays.ofDayOfWeek(CheckDaysConverter.toDayOfWeeks(korWeekDays))
            .isCheckDay(LocalDate.now());
    }

    private Goal createTodayStartGoal() {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now());
        return goal;
    }

    private Goal createFutureStartGoal() {
        Goal goal = Goal.builder()
            .period(new GoalPeriod(LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)))
            .category(GoalCategory.ETC)
            .title("futureGoal")
            .checkDays(GoalCheckDays.ofDayOfWeek(DayOfWeek.values()))
            .build();
        em.persist(goal);
        return goal;
    }

    private Mate createOngoingMate(Goal goal) {
        Mate mate = goal.createMate(createUser());
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private void createOutMate(Goal goal2) {
        Mate out = createOngoingMate(goal2);
        ReflectionTestUtils.setField(out, "status", MateStatus.OUT);
    }

    private Mate createOngoingMate(Goal goal, User user) {
        Mate mate = goal.createMate(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private User createUser() {
        User user = TestEntityFactory.user(null, UUID.randomUUID().toString());
        em.persist(user);
        return user;
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