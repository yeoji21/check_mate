package checkmate.goal.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
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
    void duplicated_dayOfWeek_throws_exception() throws Exception {
        //given
        DayOfWeek[] dayOfWeeks = new DayOfWeek[]{MONDAY, SATURDAY, FRIDAY, MONDAY};

        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> GoalCheckDays.ofDayOfWeek(dayOfWeeks));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_WEEK_DAYS);
    }

    @Test
    void getAllMatchingWeekDayValues() throws Exception {
        //given

        //when
        List<Integer> matchingValues = GoalCheckDays.getAllPossibleValues(MONDAY);

        //then
        assertThat(matchingValues).hasSize(64);
        assertTrue(matchingValues.stream()
            .allMatch(value -> GoalCheckDays.ofValue(value).isCheckDay(getMonday())));
    }

    @Test
    void isCheckDayTrue() throws Exception {
        //given
        GoalCheckDays checkDays = GoalCheckDays.ofDayOfWeek(MONDAY);

        //when
        boolean isCheckDay = checkDays.isCheckDay(getMonday());

        //then
        assertTrue(isCheckDay);
    }

    private LocalDate getMonday() {
        return LocalDate.of(2022, 10, 31);
    }
}