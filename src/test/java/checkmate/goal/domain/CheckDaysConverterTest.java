package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CheckDaysConverterTest {

    @Test
    @DisplayName("단일 요일 변환 테스트")
    void test_v1() throws Exception {
        isEqualTo("월", 1);
        isEqualTo("화", 2);
        isEqualTo("수", 4);
        isEqualTo("목", 8);
        isEqualTo("금", 16);
        isEqualTo("토", 32);
        isEqualTo("일", 64);
    }

    @Test
    @DisplayName("여러 요일 변환 테스트")
    void test_v2() throws Exception {
        isEqualTo("월화", 3);
        isEqualTo("월화수", 7);
        isEqualTo("월화수목금", 1 + 2 + 4 + 8 + 16);
        isEqualTo("월수금", 1 + 4 + 16);
        isEqualTo("화목토", 2 + 8 + 32);
        isEqualTo("토일", 32 + 64);
    }

    @Test
    @DisplayName("값에서 요일로 변환 테스트")
    void test_v3() throws Exception {
        isEqualTo(1, "월");
        isEqualTo(2, "화");
        isEqualTo(4, "수");
        isEqualTo(8, "목");
        isEqualTo(16, "금");
        isEqualTo(32, "토");
        isEqualTo(64, "일");

        isEqualTo(3, "월화");
        isEqualTo(7, "월화수");
        isEqualTo(1 + 2 + 4 + 8 + 16, "월화수목금");
        isEqualTo(1 + 4 + 16, "월수금");
        isEqualTo(2 + 8 + 32, "화목토");
        isEqualTo(32 + 64, "토일");
    }

    @Test
    @DisplayName("인증 요일 확인")
    void test_V4() throws Exception {
        LocalDate monday = LocalDate.of(2022, 10, 31);
        LocalDate tuesday = LocalDate.of(2022, 11, 1);
        LocalDate wednesday = LocalDate.of(2022, 11, 2);
        LocalDate thursday = LocalDate.of(2022, 11, 3);
        LocalDate friday = LocalDate.of(2022, 11, 4);
        LocalDate saturday = LocalDate.of(2022, 11, 5);
        LocalDate sunday = LocalDate.of(2022, 11, 6);

        isWorkingDay(1, monday);
        isNotWorkingDay(1, tuesday);
        isWorkingDay(2, tuesday);
        isWorkingDay(4, wednesday);
        isWorkingDay(8, thursday);
        isWorkingDay(16, friday);
        isWorkingDay(32, saturday);
        isWorkingDay(64, sunday);
        isNotWorkingDay(16, sunday);
    }

    private void isNotWorkingDay(int value, LocalDate date) {
        assertThat(CheckDaysConverter.isWorkingDay(value, date)).isFalse();
    }

    private void isWorkingDay(int value, LocalDate date) {
        assertThat(CheckDaysConverter.isWorkingDay(value, date)).isTrue();
    }

    private void isEqualTo(String weekDay, int value) {
        assertThat(CheckDaysConverter.toValue(weekDay)).isEqualTo(value);
    }

    private void isEqualTo(int value, String weekDays) {
        assertThat(CheckDaysConverter.toKorean(value)).isEqualTo(weekDays);
    }
}
