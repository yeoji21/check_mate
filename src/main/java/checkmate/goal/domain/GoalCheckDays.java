package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import com.mysema.commons.lang.Assert;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays {

    @Column(name = "check_days", nullable = false)
    private int checkDays;

    private GoalCheckDays(String korWeekDays) {
        validateKorWeekDayFormat(korWeekDays);
        this.checkDays = CheckDaysConverter.toValue(korWeekDays);
    }

    public static GoalCheckDays ofKorean(String korWeekDays) {
        return new GoalCheckDays(korWeekDays);
    }

    public static GoalCheckDays ofValue(int value) {
        return new GoalCheckDays(CheckDaysConverter.toKorean(value));
    }

    public static GoalCheckDays ofLocalDates(LocalDate... dates) {
        return new GoalCheckDays(Arrays.stream(dates)
            .map(CheckDaysConverter::toKorean)
            .collect(Collectors.joining()));
    }

    public static List<Integer> getAllMatchingWeekDayValues(LocalDate date) {
        return IntStream.rangeClosed(1, 128)
            .filter(weekDays -> CheckDaysConverter.isWorkingDay(weekDays, date))
            .boxed()
            .toList();
    }

    private static void validateKorWeekDayFormat(String korWeekDays) {
        if (Pattern.compile("[^월화수목금토일]").matcher(korWeekDays).find()) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_DAYS);
        }
        checkDuplicateWeekDay(korWeekDays);
    }

    private static void checkDuplicateWeekDay(String korWeekDays) {
        Assert.isTrue(
            korWeekDays.length() == new HashSet<>(List.of(korWeekDays.split(""))).size(),
            "중복 요일");
    }

    public boolean isWorkingDay(LocalDate date) {
        return CheckDaysConverter.isWorkingDay(checkDays, date);
    }

    public int toInt() {
        return checkDays;
    }

    public String toKorean() {
        return CheckDaysConverter.toKorean(checkDays);
    }

    int getWorkingDayCount(Stream<LocalDate> dateStream) {
        return (int) dateStream.filter(this::isWorkingDay).count();
    }

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
}
