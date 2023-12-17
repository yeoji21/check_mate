package checkmate.goal.domain;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CheckDaysConverter {

    public static DayOfWeek[] toDayOfWeeks(String korWeekDays) {
        return Arrays.stream(korWeekDays.split(""))
            .map(day ->
                Arrays.stream(CheckDayOfWeek.values())
                    .filter(v -> day.equals(v.getKor()))
                    .map(Enum::name)
                    .collect(Collectors.joining())
            )
            .map(DayOfWeek::valueOf)
            .toArray(DayOfWeek[]::new);
    }

    public static String toKorean(GoalCheckDays checkDays) {
        return Arrays.stream(CheckDayOfWeek.values())
            .filter(day -> isCheckDay(checkDays.getValue(), day.shift))
            .map(day -> day.kor)
            .collect(Collectors.joining());
    }

    static DayOfWeek[] toDayOfWeeks(int weekDays) {
        return Arrays.stream(CheckDayOfWeek.values())
            .filter(day -> isCheckDay(weekDays, day.shift))
            .map(day -> DayOfWeek.valueOf(day.name()))
            .toArray(DayOfWeek[]::new);
    }

    static int toValue(DayOfWeek[] dayOfWeeks) {
        int value = 0;
        for (DayOfWeek dayOfWeek : dayOfWeeks) {
            value |= (1 << CheckDayOfWeek.valueOf(dayOfWeek.toString()).shift);
        }
        return value;
    }

    static boolean isCheckDay(int weekDays, DayOfWeek dayOfWeek) {
        return isCheckDay(weekDays, CheckDayOfWeek.valueOf(dayOfWeek.toString()).shift);
    }

    private static boolean isCheckDay(int weekDays, int date) {
        return (weekDays & (1 << date)) != 0;
    }

    @RequiredArgsConstructor
    private enum CheckDayOfWeek {
        MONDAY(0, "월"),
        TUESDAY(1, "화"),
        WEDNESDAY(2, "수"),
        THURSDAY(3, "목"),
        FRIDAY(4, "금"),
        SATURDAY(5, "토"),
        SUNDAY(6, "일");
        private final int shift;
        @Getter
        private final String kor;
    }
}
