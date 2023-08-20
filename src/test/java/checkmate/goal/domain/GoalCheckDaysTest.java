package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalCheckDaysTest {

    @Test
    void getAllMatchingWeekDayValues() throws Exception {
        //given

        //when
        List<Integer> matchingValues = GoalCheckDays.getAllMatchingValues(getMonday());

        //then
        assertThat(matchingValues).hasSize(64);
        assertTrue(matchingValues.stream()
            .allMatch(value -> GoalCheckDays.ofValue(value).isCheckDay(getMonday())));
    }

    @Test
    void isCheckDayTrue() throws Exception {
        //given
        GoalCheckDays checkDays = GoalCheckDays.ofKorean("월화수목금토일");

        //when
        boolean isCheckDay = checkDays.isCheckDay(getMonday());

        //then
        assertTrue(isCheckDay);
    }

    @Test
    void isCheckDayFalse() throws Exception {
        //given
        GoalCheckDays checkDays = GoalCheckDays.ofLocalDates(today().minusDays(1));

        //when
        boolean isCheckDay = checkDays.isCheckDay(today());

        //then
        assertFalse(isCheckDay);
    }

    @Test
    @DisplayName("중복 요일 검사")
    void validateDuplicateKorWeekDays() throws Exception {
        duplicateDayThrowException("월월");
        duplicateDayThrowException("월화화");
        duplicateDayThrowException("수수수");
        duplicateDayThrowException("목금토일일");
    }

    @Test
    @DisplayName("잘못된 요일 형식 검증")
    void validateInvlidFormatKorWeekDays() throws Exception {
        invalidKorWeekDay("월하");
        invalidKorWeekDay("월화스스목금토일");
        invalidKorWeekDay("하하하");
        invalidKorWeekDay("금요일입니다");
    }

    @Test
    @DisplayName("LocalDate 사용하는 생성자")
    void convertWeekDayToCheckDayValue() throws Exception {
        isEqualTo(getMonday(), 1);
        isEqualTo(getTuesday(), 2);
        isEqualTo(getWednesday(), 4);
        isEqualTo(getThursday(), 8);
        isEqualTo(getFriday(), 16);
        isEqualTo(getSaturday(), 32);
        isEqualTo(getSunday(), 64);
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private void invalidKorWeekDay(String weekDay) {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> GoalCheckDays.ofKorean(weekDay));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_WEEK_DAYS);
    }

    private void isEqualTo(LocalDate localDate, int value) {
        assertThat(GoalCheckDays.ofLocalDates(localDate)).isEqualTo(GoalCheckDays.ofValue(value));
    }

    private void duplicateDayThrowException(String weekDays) {
        assertThrows(BusinessException.class, () -> GoalCheckDays.ofKorean(weekDays));
    }

    private LocalDate getSunday() {
        return LocalDate.of(2022, 11, 6);
    }

    private LocalDate getSaturday() {
        return LocalDate.of(2022, 11, 5);
    }

    private LocalDate getFriday() {
        return LocalDate.of(2022, 11, 4);
    }

    private LocalDate getThursday() {
        return LocalDate.of(2022, 11, 3);
    }

    private LocalDate getWednesday() {
        return LocalDate.of(2022, 11, 2);
    }

    private LocalDate getTuesday() {
        return LocalDate.of(2022, 11, 1);
    }

    private LocalDate getMonday() {
        return LocalDate.of(2022, 10, 31);
    }
}