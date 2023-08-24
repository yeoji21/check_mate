package checkmate.goal.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GoalCheckDaysTest {

    @Test
    void create_by_dayOfWeek() throws Exception {
        //given
        DayOfWeek[] dayOfWeeks = new DayOfWeek[]{MONDAY, SATURDAY, FRIDAY};

        //when
        GoalCheckDays checkDays = GoalCheckDays.ofDayOfWeek(dayOfWeeks);

        //then
        assertThat(checkDays).isNotNull();
    }

    @Test
    void getAllMatchingWeekDayValues() throws Exception {
        //given

        //when
        List<Integer> matchingValues = GoalCheckDays.getAllMatchingValues(
            getMonday().getDayOfWeek());

        //then
        assertThat(matchingValues).hasSize(64);
        assertTrue(matchingValues.stream()
            .allMatch(value -> GoalCheckDays.ofValue(value).isDateCheckDayOfWeek(getMonday())));
    }

    // TODO: 2023/08/23 중복요일 검증 테스트

    @Test
    void isCheckDayTrue() throws Exception {
        //given
        GoalCheckDays checkDays = GoalCheckDays.ofDayOfWeek(MONDAY);

        //when
        boolean isCheckDay = checkDays.isDateCheckDayOfWeek(getMonday());

        //then
        assertTrue(isCheckDay);
    }

    private LocalDate getMonday() {
        return LocalDate.of(2022, 10, 31);
    }
}