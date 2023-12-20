package checkmate.goal.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckDaysConverterTest {

    @Test
    @DisplayName("단일 요일 변환 테스트")
    void should_return_value_when_input_dayOfWeek() throws Exception {
        dayOfWeeksToValue(List.of(MONDAY), 1);
        dayOfWeeksToValue(List.of(TUESDAY), 2);
        dayOfWeeksToValue(List.of(WEDNESDAY), 4);
        dayOfWeeksToValue(List.of(THURSDAY), 8);
        dayOfWeeksToValue(List.of(FRIDAY), 16);
        dayOfWeeksToValue(List.of(SATURDAY), 32);
        dayOfWeeksToValue(List.of(SUNDAY), 64);
    }

    @Test
    @DisplayName("여러 요일 변환 테스트")
    void should_return_value_when_input_dayOfWeeks() throws Exception {
        dayOfWeeksToValue(List.of(MONDAY, TUESDAY), 3);
        dayOfWeeksToValue(List.of(MONDAY, TUESDAY, WEDNESDAY), 7);
        dayOfWeeksToValue(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY),
            1 + 2 + 4 + 8 + 16);
        dayOfWeeksToValue(List.of(MONDAY, WEDNESDAY, FRIDAY), 1 + 4 + 16);
        dayOfWeeksToValue(List.of(SATURDAY, SUNDAY), 32 + 64);
    }

    @Test
    @DisplayName("값에서 요일로 변환 테스트")
    void should_return_dayOfWeeks_when_input_value() throws Exception {
        valueToDayOfWeeks(3, MONDAY, TUESDAY);
        valueToDayOfWeeks(7, MONDAY, TUESDAY, WEDNESDAY);
        valueToDayOfWeeks(1 + 2 + 4 + 8 + 16, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        valueToDayOfWeeks(1 + 4 + 16, MONDAY, WEDNESDAY, FRIDAY);
        valueToDayOfWeeks(32 + 64, SATURDAY, SUNDAY);
    }

    @Test
    @DisplayName("value가 해당 요일의 shift 값을 포함하는 경우")
    void should_return_true_when_value_contains_dayOfWeek() throws Exception {
        isCheckDay(1, MONDAY);
        isCheckDay(2, TUESDAY);
        isCheckDay(4, WEDNESDAY);
        isCheckDay(8, THURSDAY);
        isCheckDay(16, FRIDAY);
        isCheckDay(32, SATURDAY);
        isCheckDay(64, SUNDAY);
    }

    @Test
    @DisplayName("value가 해당 요일의 shift 값을 포함하지 않는 경우")
    void should_return_false_when_value_not_contains_dayOfWeek() throws Exception {
        isNotCheckDay(1, TUESDAY);
        isNotCheckDay(16, SUNDAY);
    }


    @Test
    void should_return_korean_dayOfWeeks_when_input_GoalCheckDays() throws Exception {
        //given
        GoalCheckDays input = GoalCheckDays.ofDayOfWeek(DayOfWeek.values());

        //when
        String result = CheckDaysConverter.toKorean(input);

        //then
        assertThat(result).isEqualTo("월화수목금토일");
    }

    private void isNotCheckDay(int value, DayOfWeek dayOfWeek) {
        assertThat(CheckDaysConverter.isValueContainsDayOfWeek(value, dayOfWeek)).isFalse();
    }

    private void isCheckDay(int value, DayOfWeek dayOfWeek) {
        assertThat(CheckDaysConverter.isValueContainsDayOfWeek(value, dayOfWeek)).isTrue();
    }

    private void dayOfWeeksToValue(List<DayOfWeek> dayOfWeeks, int value) {
        assertThat(CheckDaysConverter.toValue(dayOfWeeks.toArray(DayOfWeek[]::new))).isEqualTo(
            value);
    }

    private void valueToDayOfWeeks(int value, DayOfWeek... dayOfWeeks) {
        assertThat(CheckDaysConverter.toDayOfWeeks(value)).isEqualTo(dayOfWeeks);
    }
}
