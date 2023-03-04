package checkmate.goal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalPeriodTest {
    @Test
    @DisplayName("시작일이 종료일보다 미래일 때 예외 발생")
    void invalidDateRange() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new GoalPeriod(today(), today().minusDays(1)));
    }

    @Test
    @DisplayName("목표 시작 여부 검증")
    void uninitiated() throws Exception {
        GoalPeriod tomorrowStart = new GoalPeriod(today().plusDays(1), today().plusDays(2));
        GoalPeriod todayStart = new GoalPeriod(today(), today().plusDays(2));

        assertThat(tomorrowStart.isUninitiated()).isTrue();
        assertThat(todayStart.isUninitiated()).isFalse();
    }

    @Test
    @DisplayName("목표 진행률 계산")
    void progressPercent() throws Exception {
        GoalPeriod uninitiated = new GoalPeriod(today().plusDays(1), today().plusDays(10));
        assertThat(uninitiated.calcProgressedPercent()).isEqualTo(0);

        GoalPeriod halfProgressed = new GoalPeriod(today().minusDays(10), today().plusDays(9));
        assertThat(halfProgressed.calcProgressedPercent()).isEqualTo(50);

        GoalPeriod ended = new GoalPeriod(today().minusDays(10), today().minusDays(1));
        assertThat(ended.calcProgressedPercent()).isEqualTo(100);
    }

    private LocalDate today() {
        return LocalDate.now();
    }
}