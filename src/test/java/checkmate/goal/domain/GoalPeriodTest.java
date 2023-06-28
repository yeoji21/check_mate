package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalPeriodTest {

    @Test
    @DisplayName("시작일이 종료일보다 미래일 때 예외 발생")
    void createGoalPeriodWhenInvalidDateRange() throws Exception {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> new GoalPeriod(today(), todayMinusDays(1)));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GOAL_DATE);
    }

    @Test
    void isInitiatedTrueWhenTodayStartPeriod() throws Exception {
        GoalPeriod period = new GoalPeriod(today(), todayPlusDays(2));
        assertTrue(period.isInitiated());
    }

    @Test
    void isInitiatedFalseWhenFutureStartPeriod() throws Exception {
        GoalPeriod period = new GoalPeriod(todayPlusDays(1), todayPlusDays(2));
        assertFalse(period.isInitiated());
    }

    @Test
    void isInitiatedFalseWhenPastStartPeriod() throws Exception {
        GoalPeriod period = new GoalPeriod(todayMinusDays(1), todayPlusDays(2));
        assertTrue(period.isInitiated());
    }

    @Test
    @DisplayName("목표 진행률 계산")
    void getProgressedPercentWhenUninitiated() throws Exception {
        GoalPeriod period = new GoalPeriod(todayPlusDays(1), todayPlusDays(10));
        assertThat(period.getProgressedPercent()).isZero();
    }

    @Test
    void getProgressedPercentWhenProgressed() throws Exception {
        GoalPeriod period = new GoalPeriod(todayMinusDays(10), todayPlusDays(9));
        assertThat(period.getProgressedPercent()).isEqualTo(50);
    }

    @Test
    void getProgressedPercentWhenEnded() throws Exception {
        GoalPeriod period = new GoalPeriod(todayMinusDays(10), todayMinusDays(1));
        assertThat(period.getProgressedPercent()).isEqualTo(100);
    }

    @Test
    void isBelongToPeriodTrue() throws Exception {
        //given
        GoalPeriod period = new GoalPeriod(todayMinusDays(10), todayPlusDays(9));

        //when
        boolean isBelong = period.isBelongToPeriod(today());

        //then
        assertTrue(isBelong);
    }

    @Test
    void isBelongToPeriodFalseBecausePastDate() throws Exception {
        //given
        GoalPeriod period = new GoalPeriod(today(), todayPlusDays(9));

        //when
        boolean isBelong = period.isBelongToPeriod(todayMinusDays(1));

        //then
        assertFalse(isBelong);
    }

    @Test
    void isBelongToPeriodFalseBecauseFutureDate() throws Exception {
        //given
        GoalPeriod period = new GoalPeriod(today(), today());

        //when
        boolean isBelong = period.isBelongToPeriod(todayPlusDays(1));

        //then
        assertFalse(isBelong);
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