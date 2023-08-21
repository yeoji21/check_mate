package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.DayOfWeek;
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
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

// TODO: 2023/08/20 KorWeekDay -> DayOfWeek로 변경 고려
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays {

    @Column(name = "check_days", nullable = false)
    private int checkDays;

    private GoalCheckDays(String korWeekDays) {
        if (isContainsInvalidKorWeekDay(korWeekDays) || isContainsDuplicatedWeekDay(korWeekDays)) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_DAYS);
        }
        this.checkDays = CheckDaysConverter.toValue(korWeekDays);
    }

    public static GoalCheckDays ofKorean(String korWeekDays) {
        return new GoalCheckDays(korWeekDays);
    }

    public static GoalCheckDays ofDayOfWeek(DayOfWeek... dayOfWeeks) {
        String korWeekDays = Arrays.stream(dayOfWeeks)
            .map(day -> CheckDaysConverter.valueOf(day.toString()).kor)
            .collect(Collectors.joining());
        return new GoalCheckDays(korWeekDays);
    }

    public static GoalCheckDays ofValue(int value) {
        return new GoalCheckDays(CheckDaysConverter.toKorean(value));
    }

    public static List<Integer> getAllMatchingValues(LocalDate date) {
        return IntStream.rangeClosed(1, 128)
            .filter(weekDays -> CheckDaysConverter.isCheckDay(weekDays, date))
            .boxed()
            .toList();
    }

    private static boolean isContainsInvalidKorWeekDay(String korWeekDays) {
        return Pattern.compile("[^월화수목금토일]").matcher(korWeekDays).find();
    }

    private static boolean isContainsDuplicatedWeekDay(String korWeekDays) {
        return korWeekDays.length() != new HashSet<>(List.of(korWeekDays.split(""))).size();
    }

    public boolean isCheckDay(LocalDate date) {
        return CheckDaysConverter.isCheckDay(checkDays, date);
    }

    public String toKorean() {
        return CheckDaysConverter.toKorean(checkDays);
    }

    // TODO: 2023/08/20 접근제한자 수정해서 캡슐화해야 함
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
            Stream.of(values()).collect(Collectors.toMap(v -> v.kor, e -> e));
        private final int shift;
        private final String kor;

        public static String toKorean(LocalDate... dates) {
            return Arrays.stream(dates)
                .map(date -> valueOf(date).kor)
                .collect(Collectors.joining());
        }

        static String toKorean(int weekDays) {
            return Arrays.stream(values())
                .filter(day -> isCheckDay(weekDays, day.shift))
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

        static CheckDaysConverter valueOf(LocalDate date) {
            return CheckDaysConverter.valueOf(date.getDayOfWeek().toString());
        }

        static boolean isCheckDay(int weekDays, LocalDate date) {
            return isCheckDay(weekDays, valueOf(date).shift);
        }

        private static boolean isCheckDay(int weekDays, int date) {
            return (weekDays & (1 << date)) != 0;
        }
    }
}
