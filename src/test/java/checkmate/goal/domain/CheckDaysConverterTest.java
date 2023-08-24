package checkmate.goal.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

import checkmate.goal.domain.GoalCheckDays.CheckDaysConverter;
import java.time.DayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckDaysConverterTest {

    @Test
    @DisplayName("단일 요일 변환 테스트")
    void test_v1() throws Exception {
        dayOfWeeksToValue(1, MONDAY);
        dayOfWeeksToValue(2, TUESDAY);
        dayOfWeeksToValue(4, WEDNESDAY);
        dayOfWeeksToValue(8, THURSDAY);
        dayOfWeeksToValue(16, FRIDAY);
        dayOfWeeksToValue(32, SATURDAY);
        dayOfWeeksToValue(64, SUNDAY);
    }

    @Test
    @DisplayName("여러 요일 변환 테스트")
    void test_v2() throws Exception {
        dayOfWeeksToValue(3, MONDAY, TUESDAY);
        dayOfWeeksToValue(7, MONDAY, TUESDAY, WEDNESDAY);
        dayOfWeeksToValue(1 + 2 + 4 + 8 + 16, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        dayOfWeeksToValue(1 + 4 + 16, MONDAY, WEDNESDAY, FRIDAY);
        dayOfWeeksToValue(32 + 64, SATURDAY, SUNDAY);
    }

    @Test
    @DisplayName("값에서 요일로 변환 테스트")
    void test_v3() throws Exception {
        valueToDayOfWeeks(3, MONDAY, TUESDAY);
        valueToDayOfWeeks(7, MONDAY, TUESDAY, WEDNESDAY);
        valueToDayOfWeeks(1 + 2 + 4 + 8 + 16, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        valueToDayOfWeeks(1 + 4 + 16, MONDAY, WEDNESDAY, FRIDAY);
        valueToDayOfWeeks(32 + 64, SATURDAY, SUNDAY);
    }

    @Test
    @DisplayName("인증 요일 확인")
    void test_V4() throws Exception {
        isCheckDay(1, MONDAY);
        isNotCheckDay(1, TUESDAY);
        isCheckDay(2, TUESDAY);
        isCheckDay(4, WEDNESDAY);
        isCheckDay(8, THURSDAY);
        isCheckDay(16, FRIDAY);
        isCheckDay(32, SATURDAY);
        isCheckDay(64, SUNDAY);
        isNotCheckDay(16, SUNDAY);
    }

    private void isNotCheckDay(int value, DayOfWeek dayOfWeek) {
        assertThat(CheckDaysConverter.isCheckDayOfWeek(value, dayOfWeek)).isFalse();
    }

    private void isCheckDay(int value, DayOfWeek dayOfWeek) {
        assertThat(CheckDaysConverter.isCheckDayOfWeek(value, dayOfWeek)).isTrue();
    }

    private void dayOfWeeksToValue(int value, DayOfWeek... dayOfWeeks) {
        assertThat(CheckDaysConverter.toValue(dayOfWeeks)).isEqualTo(value);
    }

    private void valueToDayOfWeeks(int value, DayOfWeek... dayOfWeeks) {
        assertThat(CheckDaysConverter.toDayOfWeeks(value)).isEqualTo(dayOfWeeks);
    }
}
