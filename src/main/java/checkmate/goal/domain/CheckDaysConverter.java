package checkmate.goal.domain;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum CheckDaysConverter {
    MONDAY(0, "월"),
    TUESDAY(1, "화"),
    WEDNESDAY(2, "수"),
    THURSDAY(3, "목"),
    FRIDAY(4, "금"),
    SATURDAY(5, "토"),
    SUNDAY(6, "일");
    private final int value;
    private final String kor;
    private static final Map<String, CheckDaysConverter> KOR_MAP =
            Stream.of(values()).collect(Collectors.toMap(CheckDaysConverter::getKor, e -> e));

    public static int toValue(String korWeekDays) {
        int value = 0;
        for (String weekDay : korWeekDays.split("")) {
            value |= (1 << KOR_MAP.get(weekDay).value);
        }
        return value;
    }

    public static String toDays(int value) {
        return Arrays.stream(values())
                .filter(day -> isWorkingDay(value, day.value))
                .map(day -> day.kor)
                .collect(Collectors.joining());
    }

    private static boolean isWorkingDay(int value, int weekDays) {
        return (value & (1 << weekDays)) != 0;
    }

    public static boolean isWorkingDay(int value, LocalDate date) {
        return isWorkingDay(value, CheckDaysConverter.valueOf(date.getDayOfWeek().toString()).value);
    }

    public String getKor() {
        return kor;
    }
}
