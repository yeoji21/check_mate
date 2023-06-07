package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalCheckDaysTest {

    @Test
    @DisplayName("LocalDate 사용하는 생성자")
    void constructor_v2() throws Exception {
        LocalDate monday = LocalDate.of(2022, 10, 31);
        LocalDate tuesday = LocalDate.of(2022, 11, 1);
        LocalDate wednesday = LocalDate.of(2022, 11, 2);
        LocalDate thursday = LocalDate.of(2022, 11, 3);
        LocalDate friday = LocalDate.of(2022, 11, 4);
        LocalDate saturday = LocalDate.of(2022, 11, 5);
        LocalDate sunday = LocalDate.of(2022, 11, 6);

        isEqualTo(monday, 1);
        isEqualTo(tuesday, 2);
        isEqualTo(wednesday, 4);
        isEqualTo(thursday, 8);
        isEqualTo(friday, 16);
        isEqualTo(saturday, 32);
        isEqualTo(sunday, 64);
    }

    @Test
    @DisplayName("중복 요일 검사")
    void duplicateDayCheck() throws Exception {
        duplicateDayThrowException("월월");
        duplicateDayThrowException("월화화");
        duplicateDayThrowException("수수수");
        duplicateDayThrowException("목금토일일");
    }

    @Test
    @DisplayName("잘못된 요일 형식 검증")
    void regex() throws Exception {
        invalidKorWeekDay("월하");
        invalidKorWeekDay("월화스스목금토일");
        invalidKorWeekDay("하하하");
        invalidKorWeekDay("금요일입니다");
    }

    private void invalidKorWeekDay(String weekDay) {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> new GoalCheckDays(weekDay));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_WEEK_DAYS);
    }

    private void isEqualTo(LocalDate localDate, int value) {
        assertThat(new GoalCheckDays(CheckDaysConverter.toKorWeekDay(localDate)).intValue())
            .isEqualTo(value);
    }

    private void duplicateDayThrowException(String weekDays) {
        assertThrows(IllegalArgumentException.class, () -> new GoalCheckDays(weekDays));
    }
}