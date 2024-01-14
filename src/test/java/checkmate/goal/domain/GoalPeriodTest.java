package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GoalPeriodTest {

    @Test
    void should_throw_exception_when_create_with_invalid_date() throws Exception {
        //given
        LocalDate startDate = today();
        LocalDate endDate = todayMinusDays(1);

        //when
        Executable executable = () -> new GoalPeriod(startDate, endDate);

        //then
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GOAL_DATE);
    }

    @Test
    void should_contains_true_when_input_later_than_startDate_and_eariler_than_endDate()
        throws Exception {
        //given
        LocalDate input = today();
        GoalPeriod sut = new GoalPeriod(todayMinusDays(1), todayPlusDays(1));

        //when
        boolean result = sut.contains(input);

        //then
        assertTrue(result);
    }

    @Test
    void should_contains_true_when_input_equal_to_startDate_and_eariler_than_endDate()
        throws Exception {
        //given
        LocalDate input = today();
        GoalPeriod sut = new GoalPeriod(today(), todayPlusDays(1));

        //when
        boolean result = sut.contains(input);

        //then
        assertTrue(result);
    }

    @Test
    void should_contains_true_when_input_later_than_startDate_and_equal_to_endDate()
        throws Exception {
        //given
        LocalDate input = today();
        GoalPeriod sut = new GoalPeriod(todayMinusDays(1), today());

        //when
        boolean result = sut.contains(input);

        //then
        assertTrue(result);
    }

    @Test
    void should_contains_false_when_input_eariler_than_startDate() throws Exception {
        //given
        LocalDate input = todayMinusDays(1);
        GoalPeriod sut = new GoalPeriod(today(), todayPlusDays(1));

        //when
        boolean result = sut.contains(input);

        //then
        assertFalse(result);
    }

    @Test
    void should_contains_false_when_input_later_than_endDate() throws Exception {
        //given
        LocalDate input = todayPlusDays(1);
        GoalPeriod sut = new GoalPeriod(today(), today());

        //when
        boolean result = sut.contains(input);

        //then
        assertFalse(result);
    }

    @Test
    void should_started_true_when_today_equal_to_startDate() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(today(), todayPlusDays(2));

        //when
        boolean result = sut.isStarted();

        //then
        assertThat(result).isTrue();
    }

    @Test
    void should_started_true_when_today_later_than_startDate() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayMinusDays(1), todayPlusDays(2));

        //when
        boolean result = sut.isStarted();

        //then
        assertThat(result).isTrue();
    }

    @Test
    void should_started_false_when_today_eariler_than_startDate() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayPlusDays(1), todayPlusDays(2));

        //when
        boolean result = sut.isStarted();

        //then
        assertThat(result).isFalse();
    }

    @Test
    void should_0_percent_when_today_eariler_than_startDate() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayPlusDays(1), todayPlusDays(10));

        //when
        double result = sut.getProgressedPercent();

        //then
        assertThat(result).isZero();
    }

    @Test
    void should_50_percent_when_progressed_10_days_for_20_days() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayMinusDays(10), todayPlusDays(9));

        //when
        double result = sut.getProgressedPercent();

        //then
        assertThat(result).isEqualTo(50.0);
    }

    @Test
    void should_45_percent_when_progressed_9_days_for_20_days() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayMinusDays(9), todayPlusDays(10));

        //when
        double result = sut.getProgressedPercent();

        //then
        assertThat(result).isEqualTo(45.0);
    }

    @Test
    void should_100_percent_when_today_later_than_endDate() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayMinusDays(10), todayMinusDays(1));

        //when
        double result = sut.getProgressedPercent();

        //then
        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void should_contains_startDate_and_endDate_when_full_period() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayMinusDays(9), todayPlusDays(10));

        //when
        Stream<LocalDate> result = sut.getFullPeriodStream();

        //then
        Set<LocalDate> resultSet = result.collect(Collectors.toSet());
        assertThat(resultSet).hasSize(20);
        assertThat(resultSet).contains(todayMinusDays(9), todayPlusDays(10));
    }

    @Test
    void should_not_contains_today_when_until_today_period() throws Exception {
        //given
        GoalPeriod sut = new GoalPeriod(todayMinusDays(9), todayPlusDays(10));

        //when
        Stream<LocalDate> result = sut.getUntilTodayPeriodStream();

        //then
        Set<LocalDate> resultSet = result.collect(Collectors.toSet());
        assertThat(resultSet).hasSize(9);
        assertThat(resultSet).contains(todayMinusDays(9), todayMinusDays(1));
        assertThat(resultSet).doesNotContain(today());
    }

    private LocalDate todayPlusDays(int day) {
        return today().plusDays(day);
    }

    private LocalDate todayMinusDays(int day) {
        return today().minusDays(day);
    }

    private LocalDate today() {
        return LocalDate.now();
    }
}