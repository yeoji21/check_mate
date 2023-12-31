package checkmate.goal.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.DayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GoalCheckDaysTest {

    @DisplayName("DayOfWeek 값을 통해 GoalCheckDays 객체 생성")
    @Test
    void should_return_GoalCheckDays_when_input_dayOfWeeks() throws Exception {
        //given
        DayOfWeek[] input = new DayOfWeek[]{MONDAY, SATURDAY, FRIDAY};

        //when
        GoalCheckDays result = GoalCheckDays.ofDayOfWeek(input);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(CheckDaysConverter.toValue(input));
    }

    @DisplayName("GoalCheckDays 객체 생성 시 중복된 요일이 포함된 경우, 예외 발생")
    @Test
    void should_throws_exception_when_input_contains_duplicated_dayOfWeeks() throws Exception {
        //given
        DayOfWeek[] dayOfWeeks = new DayOfWeek[]{MONDAY, SATURDAY, FRIDAY, MONDAY};

        //when
        Executable executable = () -> GoalCheckDays.ofDayOfWeek(dayOfWeeks);

        //then
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_WEEK_DAYS);
    }
}