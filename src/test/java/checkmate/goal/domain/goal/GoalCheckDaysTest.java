package checkmate.goal.domain.goal;

import checkmate.goal.domain.GoalCheckDays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalCheckDaysTest {
    @Test @DisplayName("LocalDate 사용하는 생성자")
    void constructor_v2() throws Exception{
        LocalDate monday = LocalDate.of(2022, 10, 31);
        LocalDate tuesday = LocalDate.of(2022, 11, 1);
        LocalDate wednesday = LocalDate.of(2022, 11, 2);
        LocalDate thursday = LocalDate.of(2022, 11, 3);
        LocalDate friday = LocalDate.of(2022, 11, 4);
        LocalDate saturday = LocalDate.of(2022, 11, 5);
        LocalDate sunday = LocalDate.of(2022, 11, 6);

        isEqualTo(List.of(monday), 1);
        isEqualTo(List.of(tuesday), 2);
        isEqualTo(List.of(wednesday), 4);
        isEqualTo(List.of(thursday), 8);
        isEqualTo(List.of(friday), 16);
        isEqualTo(List.of(saturday), 32);
        isEqualTo(List.of(sunday), 64);
    }

    @Test @DisplayName("중복 요일 검사")
    void duplicateDayCheck() throws Exception{
        duplicateDayThrowException("월월");
        duplicateDayThrowException("월화화");
        duplicateDayThrowException("수수수");
        duplicateDayThrowException("목금토일일");
    }

    @Test
    void regex() throws Exception{
        assertThat(getMatcher("월하").find()).isTrue();
        assertThat(getMatcher("월화수목금").find()).isFalse();
        assertThat(getMatcher("월화스스목금토일").find()).isTrue();
        assertThat(getMatcher("토일").find()).isFalse();
        assertThat(getMatcher("일").find()).isFalse();
        assertThat(getMatcher("월수금").find()).isFalse();
    }

    private void isEqualTo(List<LocalDate> localDates, int value) {
        assertThat(new GoalCheckDays(localDates).intValue()).isEqualTo(value);
    }

    private void duplicateDayThrowException(String weekDays) {
        assertThrows(IllegalArgumentException.class, () -> new GoalCheckDays(weekDays));
    }

    private Matcher getMatcher(String days) {
        String regex = "[^월화수목금토일]";
        return Pattern.compile(regex).matcher(days);
    }
}