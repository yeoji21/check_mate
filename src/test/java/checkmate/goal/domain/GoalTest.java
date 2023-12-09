package checkmate.goal.domain;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.test.util.ReflectionTestUtils;

class GoalTest {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    void modified_within_7_days_throw_exception() throws Exception {
        //given
        Goal sut = fixtureMonkey
            .giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getModifiedDateTime), Arbitraries.randomValue(random -> {
                int daysToMinus = random.nextInt() % 7;
                return LocalDateTime.now().minusDays(daysToMinus);
            }))
            .sample();

        //when
        Executable executable = () -> sut.modify(fixtureMonkey.giveMeOne(GoalModifyEvent.class));

        //then
        ErrorCode errorCode = assertThrows(BusinessException.class, executable).getErrorCode();
        assertThat(errorCode).isEqualTo(ErrorCode.UPDATE_DURATION);
    }

    @Test
    void modified_after_7_days_later() throws Exception {
        //given
        Goal sut = fixtureMonkey
            .giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getModifiedDateTime), Arbitraries.randomValue(random -> {
                int daysToMinus = random.nextInt() % 7;
                return LocalDateTime.now().minusDays(Math.abs(daysToMinus) + 7);
            }))
            .sample();

        //when
        Executable executable = () -> sut.modify(new GoalModifyEvent());

        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void modify_endDate_to_later() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(LocalDate.now(), LocalDate.now()))
            .setNull(javaGetter(Goal::getModifiedDateTime))
            .sample();
        GoalModifyEvent event = fixtureMonkey
            .giveMeBuilder(GoalModifyEvent.class)
            .set(javaGetter(GoalModifyEvent::getEndDate), sut.getEndDate().plusDays(1))
            .sample();

        //when
        sut.modify(event);

        //then
        assertThat(sut.getEndDate()).isEqualTo(event.getEndDate());
    }

    @Test
    void modify_endDate_to_earlier() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(LocalDate.now(), LocalDate.now()))
            .setNull(javaGetter(Goal::getModifiedDateTime))
            .sample();
        GoalModifyEvent event = fixtureMonkey
            .giveMeBuilder(GoalModifyEvent.class)
            .set(javaGetter(GoalModifyEvent::getEndDate), sut.getEndDate().minusDays(1))
            .sample();

        //when
        Executable executable = () -> sut.modify(event);

        //then
        ErrorCode errorCode = assertThrows(BusinessException.class, executable).getErrorCode();
        assertThat(errorCode).isEqualTo(ErrorCode.INVALID_GOAL_DATE);
    }

    @Test
    void remove_appointmentTime() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .setNull(javaGetter(Goal::getModifiedDateTime))
            .setNotNull(javaGetter(Goal::getAppointmentTime))
            .sample();
        GoalModifyEvent event = fixtureMonkey.giveMeBuilder(GoalModifyEvent.class)
            .setNull(javaGetter(GoalModifyEvent::getEndDate))
            .set(javaGetter(GoalModifyEvent::isTimeReset), true)
            .sample();

        //when
        sut.modify(event);

        //then
        assertThat(sut.getAppointmentTime()).isNull();
    }

    @Test
    void modify_appointmentTime() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .setNull(javaGetter(Goal::getModifiedDateTime))
            .setNotNull(javaGetter(Goal::getAppointmentTime))
            .sample();

        GoalModifyEvent event = fixtureMonkey.giveMeBuilder(GoalModifyEvent.class)
            .setNull(javaGetter(GoalModifyEvent::getEndDate))
            .set(javaGetter(GoalModifyEvent::isTimeReset), false)
            .setNotNull(javaGetter(GoalModifyEvent::getAppointmentTime))
            .sample();

        //when
        sut.modify(event);

        //then
        assertThat(sut.getAppointmentTime()).isEqualTo(event.getAppointmentTime());
    }

    @Test
    void isTimeOverTrue() throws Exception {
        //given
        Goal goal = createGoalWithAppointmentTime(LocalTime.MIN);

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isTrue();
        assertThat(goal.getAppointmentTime().isBefore(LocalTime.now())).isTrue();
    }

    @Test
    void isTimeOverFalse() throws Exception {
        //given
        Goal goal = createGoalWithAppointmentTime(LocalTime.MAX);

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isFalse();
        assertThat(goal.getAppointmentTime().isAfter(LocalTime.now())).isTrue();
    }

    @Test
    void isTimeOverWhenEmptyAppointmentTime() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isFalse();
    }

    @Test
    @DisplayName("땡땡이 최대치 조회")
    void getSkippedDayLimit() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "period", new GoalPeriod(
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(10)));

        //when
        int skippedDayLimit = goal.getSkippedDayLimit();

        //then
        assertThat(skippedDayLimit).isEqualTo(3);
    }

    @Test
    @DisplayName("목표 초대 가능 여부 - 초대 가능")
    void isInviteableTrue() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        boolean inviteable = goal.isInviteable();

        // then
        assertThat(inviteable).isTrue();
        assertThat(goal.getPeriod().getProgressedPercent()).isLessThan(
            GoalPolicyConstants.INVITE_MAX_ACCEPTABLE_PERCENT);
    }

    @Test
    @DisplayName("목표 초대 가능 여부 - 초대 불가능")
    void isInviteableFalseBecauseProgressPercent() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            LocalDate.now().minusDays(200L));

        //when
        boolean inviteable = goal.isInviteable();

        // then
        assertThat(inviteable).isFalse();
    }

    @Test
    @DisplayName("목표 진행 일 수")
    void getProgressedCheckDayCount() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));
        ReflectionTestUtils.setField(goal, "checkDays",
            GoalCheckDays.ofDayOfWeek(DayOfWeek.values()));

        //when
        int progressedCheckDayCount = goal.getProgressedCheckDayCount();

        //then
        assertThat(progressedCheckDayCount).isEqualTo(10);
    }

    @Test
    void isTodayCheckDayTrue() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "checkDays",
            GoalCheckDays.ofDayOfWeek(DayOfWeek.values()));

        //when
        boolean isCheckDay = goal.isTodayCheckDay();

        //then
        assertThat(isCheckDay).isTrue();
    }

    @Test
    void isTodayCheckDayFalse() throws Exception {
        //given
        Goal goal = createGoal();
        GoalCheckDays checkDays = GoalCheckDays.ofDayOfWeek(getYesterdayDayOfWeek());
        ReflectionTestUtils.setField(goal, "checkDays", checkDays);

        //when
        boolean isCheckDay = goal.isTodayCheckDay();

        //then
        assertThat(isCheckDay).isFalse();
    }

    private DayOfWeek getYesterdayDayOfWeek() {
        return LocalDate.now().minusDays(1).getDayOfWeek();
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }

    private Goal createGoalWithAppointmentTime(LocalTime appointmentTime) {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "appointmentTime", appointmentTime);
        return goal;
    }

}