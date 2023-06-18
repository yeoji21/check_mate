package checkmate.goal.domain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CheckDaysConverter {
    MONDAY(0, "월"),
    TUESDAY(1, "화"),
    WEDNESDAY(2, "수"),
    THURSDAY(3, "목"),
    FRIDAY(4, "금"),
    SATURDAY(5, "토"),
    SUNDAY(6, "일");
    private static final Map<String, CheckDaysConverter> KOR_MAP =
        Stream.of(values()).collect(Collectors.toMap(CheckDaysConverter::getKor, e -> e));
    private final int shift;
    @Getter(AccessLevel.PRIVATE)
    private final String kor;

    static String toKorean(LocalDate... dates) {
        return Arrays.stream(dates)
            .map(date -> valueOf(date).kor)
            .collect(Collectors.joining());
    }

    static String toKorean(int weekDays) {
        return Arrays.stream(values())
            .filter(day -> isWorkingDay(weekDays, day.shift))
            .map(day -> day.kor)
            .collect(Collectors.joining());
    }

    static int toValue(String korWeekDays) {
        int weekDays = 0;
        for (String weekDay : korWeekDays.split("")) {
            weekDays |= (1 << KOR_MAP.get(weekDay).shift);
        }
        return weekDays;
    }

    static boolean isWorkingDay(int weekDays, LocalDate date) {
        return isWorkingDay(weekDays, valueOf(date).shift);
    }

    static CheckDaysConverter valueOf(LocalDate date) {
        return CheckDaysConverter.valueOf(date.getDayOfWeek().toString());
    }

    private static boolean isWorkingDay(int weekDays, int date) {
        return (weekDays & (1 << date)) != 0;
    }
}
