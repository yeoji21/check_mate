package checkmate.goal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalPeriodTest {

    @Test @DisplayName("시작일이 종료일보다 미래일 때 예외 발생")
    void createError() throws Exception{
        assertThrows(IllegalArgumentException.class,
                () -> new GoalPeriod(LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test @DisplayName("시작일 검증")
    void uninitiated() throws Exception{
        assertThat(new GoalPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
                .isUninitiated()).isTrue();
        assertThat(new GoalPeriod(LocalDate.now(), LocalDate.now().plusDays(2))
                .isUninitiated()).isFalse();
    }

    @Test
    void progressPercent() throws Exception{
        GoalPeriod uninitiated = new GoalPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
        assertThat(uninitiated.calcProgressedPercent()).isEqualTo(0);
        GoalPeriod halfProgressed = new GoalPeriod(LocalDate.now().minusDays(10), LocalDate.now().plusDays(9));
        assertThat(halfProgressed.calcProgressedPercent()).isEqualTo(50);
        GoalPeriod ended = new GoalPeriod(LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
        assertThat(ended.calcProgressedPercent()).isEqualTo(100);
    }
}