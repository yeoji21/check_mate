package checkmate.goal.domain;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

class GoalTest {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .build();

    @DisplayName("7일 이내에 목표를 수정했을 경우, 목표를 수정할 수 없음")
    @Test
    void should_throw_exception_when_modified_within_7_days() throws Exception {
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

    @DisplayName("목표를 수정한지 7일 이상이면, 목표 수정 가능")
    @Test
    void should_not_throw_exception_modified_after_7_days() throws Exception {
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

    @DisplayName("목표 종료일을 기존보다 이전 날짜로 수정할 수 없음")
    @Test
    void should_throw_exception_when_modify_to_earlier_endDate() throws Exception {
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

    @DisplayName("목표 종료일을 기존보다 이후 날짜로 수정")
    @Test
    void should_modify_endDate_when_modify_to_after_endDate() throws Exception {
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

    @DisplayName("설정된 목표 인증 시간을 제거")
    @Test
    void should_remove_appointmentTime_when_timeReset_true() throws Exception {
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

    @DisplayName("설정된 목표 인증 시간을 변경")
    @Test
    void should_modify_appointmentTime_when_modify_appointmentTime() throws Exception {
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

    @DisplayName("현재 시간이 목표 인증 시간보다 이후")
    @Test
    void should_true_when_appointmentTime_is_before_than_now() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getAppointmentTime), LocalTime.MIN)
            .sample();

        //when
        boolean result = sut.isAppointmentTimeOver();

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("현재 시간이 목표 인증 시간보다 이전")
    @Test
    void should_false_when_appointmentTime_is_after_than_now() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getAppointmentTime), LocalTime.MAX)
            .sample();

        //when
        boolean result = sut.isAppointmentTimeOver();

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("매일, 20일간 진행하는 목표의 경우, skippedDay 제한은 3")
    @Test
    void should_3_when_20_days_period_and_everyday_checkday() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(9)))
            .set(javaGetter(Goal::getCheckDays), GoalCheckDays.ofDayOfWeek(DayOfWeek.values()))
            .sample();

        //when
        int result = sut.getLimitOfSkippedDay();

        //then
        assertThat(result).isEqualTo(3);
    }

    @DisplayName("주 1회, 20일간 진행하는 목표의 경우, skippedDay 제한은 1")
    @Test
    void should_1_when_20_days_period_and_one_checkday() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(9)))
            .set(javaGetter(Goal::getCheckDays), GoalCheckDays.ofDayOfWeek(DayOfWeek.MONDAY))
            .sample();

        //when
        int result = sut.getLimitOfSkippedDay();

        //then
        assertThat(result).isEqualTo(1);
    }

    @DisplayName("초대 가능 진행률을 초과하지 않으면, 목표로 초대 가능함")
    @Test
    void should_true_when_not_exceed_acceptable_progressed_percent() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(10)))
            .sample();

        //when
        boolean result = sut.isInviteable();

        // then
        assertThat(result).isTrue();
        assertThat(sut.getPeriod().getProgressedPercent())
            .isLessThanOrEqualTo(GoalPolicyConstants.INVITE_ACCEPTABLE_PROGRESSED_PERCENT_LIMIT);
    }

    @DisplayName("초대 가능 진행률을 초과한 경우, 목표로 초대 불가")
    @Test
    void should_false_when_exceed_acceptable_progressed_percent() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(4),
                LocalDate.now().plusDays(10)))
            .sample();

        //when
        boolean result = sut.isInviteable();

        // then
        assertThat(result).isFalse();
        assertThat(sut.getPeriod().getProgressedPercent())
            .isGreaterThan(GoalPolicyConstants.INVITE_ACCEPTABLE_PROGRESSED_PERCENT_LIMIT);
    }

    @DisplayName("주7회, 10일 전에 시작한 목표의 진행된 목표 인증일은 10")
    @Test
    void should_10_when_10_days_ago_started_everyday_checkday() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(9)))
            .set(javaGetter(Goal::getCheckDays), GoalCheckDays.ofDayOfWeek(DayOfWeek.values()))
            .sample();

        //when
        int result = sut.getProgressedCheckDayCount();

        //then
        assertThat(result).isEqualTo(10);
    }

    @DisplayName("주1회, 10일 전에 시작한 목표의 진행된 목표 인증일은 1 혹은 2")
    @Test
    void should_1_or_2_when_10_days_ago_started_one_checkday() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(9)))
            .set(javaGetter(Goal::getCheckDays), GoalCheckDays.ofDayOfWeek(DayOfWeek.MONDAY))
            .sample();

        //when
        int result = sut.getProgressedCheckDayCount();

        //then
        assertThat(result).isIn(1, 2);
    }

    @DisplayName("오늘이 목표 진행 기간에 속하고 인증 요일이면 목표 인증일임")
    @Test
    void should_true_when_today_checkday_and_today_in_period() throws Exception {
        //given
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now(), LocalDate.now()))
            .set(javaGetter(Goal::getCheckDays),
                GoalCheckDays.ofDayOfWeek(today))
            .sample();

        //when
        boolean result = sut.isTodayCheckDay();

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("오늘이 목표 진행 기간에 속하지만 인증 요일이 아니면 목표 인증일이 아님")
    @Test
    void should_false_when_today_in_period_but_yesterday_checkday() throws Exception {
        //given
        DayOfWeek yesterday = LocalDate.now().minusDays(1).getDayOfWeek();
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now(), LocalDate.now()))
            .set(javaGetter(Goal::getCheckDays),
                GoalCheckDays.ofDayOfWeek(yesterday))
            .sample();

        //when
        boolean result = sut.isTodayCheckDay();

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("오늘이 목표 진행 기간에 속하지 않으면 인증 요일이여도 목표 인증일이 아님")
    @Test
    void should_false_when_everyday_checkday_but_today_not_in_goal_period() throws Exception {
        //given
        Goal sut = fixtureMonkey.giveMeBuilder(Goal.class)
            .set(javaGetter(Goal::getPeriod), new GoalPeriod(
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(1)))
            .set(javaGetter(Goal::getCheckDays),
                GoalCheckDays.ofDayOfWeek(DayOfWeek.values()))
            .sample();

        //when
        boolean result = sut.isTodayCheckDay();

        //then
        assertThat(result).isFalse();
    }

}