package checkmate.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum WeekDayConverter {
    MONDAY(1000000, "월"),
    TUESDAY(100000, "화"),
    WEDNESDAY( 10000, "수"),
    THURSDAY(1000, "목"),
    FRIDAY( 100, "금"),
    SATURDAY(10, "토"),
    SUNDAY(1, "일");
    private final int value;
    private final String kor;

    private static final Map<String, WeekDayConverter> KOR_MAP =
            Stream.of(values()).collect(Collectors.toMap(WeekDayConverter::getKor, e -> e));

    public static boolean isWorkingDay(int target, int weekDay) {
        return target / weekDay % 10 == 1;
    }

    public static String valueToKorWeekDay(int value) {
        StringBuilder result = new StringBuilder();

        for (int idx = 0, weekDayValue = 1000000; weekDayValue >= 1; weekDayValue /= 10, idx++) {
            if (isWorkingDay(value, weekDayValue)) {
                value %= weekDayValue;
                result.append(WeekDayConverter.values()[idx].getKor());
            }
        }
        return result.toString();
    }

    public static int localDateToValue(LocalDate weekDay) {
        return WeekDayConverter.valueOf(weekDay.getDayOfWeek().toString()).value;
    }

    public static String convertEngToKor(LocalDate localDate) {
        return WeekDayConverter.valueOf(localDate.getDayOfWeek().toString()).getKor();
    }

    public static String convertKorToEng(String korDay) {
        return KOR_MAP.get(korDay).name();
    }
}
