package checkmate.goal.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GoalSchedulerTest {

    @Test
    void createGoalSchedule() throws Exception {
        //given
        GoalPeriod period = new GoalPeriod(todayMinusDays(6), today());
        GoalCheckDays checkDays = GoalCheckDays.ofDayOfWeek(MONDAY, WEDNESDAY, FRIDAY);

        //when
        String goalSchedule = GoalScheduler.getTotalSchedule(period, checkDays);

        //then
        assertThat(goalSchedule).hasSize(7);
        assertThat(sum(goalSchedule.toCharArray())).isEqualTo(3);
    }

    @Test
    void createCheckedSchedule() throws Exception {
        //given
        GoalPeriod period = new GoalPeriod(todayMinusDays(6), today());
        GoalCheckDays checkDays = GoalCheckDays.ofDayOfWeek(
            todayMinusDays(2).getDayOfWeek(),
            todayMinusDays(1).getDayOfWeek(),
            today().getDayOfWeek()
        );
        List<LocalDate> checkedDates = List.of(todayMinusDays(1), today());

        //when
        String checkedSchedule = GoalScheduler.getCheckedSchedule(period, checkDays,
            checkedDates);

        //then
        assertThat(checkedSchedule).hasSize(7);
        assertThat(sum(checkedSchedule.toCharArray())).isEqualTo(2);
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private LocalDate todayMinusDays(int minusDays) {
        return LocalDate.now().minusDays(minusDays);
    }

    private int sum(char[] chars) {
        int sum = 0;
        for (char ch : chars) {
            sum += ch - '0';
        }
        return sum;
    }
}