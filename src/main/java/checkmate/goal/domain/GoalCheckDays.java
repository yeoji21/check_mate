package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

// TODO: 2023/08/24 요일별 boolean 필드 두는 방식 고려
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays {

    @Column(name = "check_days", nullable = false)
    private int checkDays;

    private GoalCheckDays(DayOfWeek[] dayOfWeeks) {
        if (isContainsDuplicatedWeekDay(dayOfWeeks)) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_DAYS);
        }
        this.checkDays = CheckDaysConverter.toValue(dayOfWeeks);
    }

    public static GoalCheckDays ofDayOfWeek(DayOfWeek... dayOfWeeks) {
        return new GoalCheckDays(dayOfWeeks);
    }

    public static GoalCheckDays ofValue(int value) {
        return new GoalCheckDays(CheckDaysConverter.toDayOfWeeks(value));
    }

    public static List<Integer> getAllMatchingValues(DayOfWeek dayOfWeek) {
        return IntStream.rangeClosed(1, 128)
            .filter(weekDays -> CheckDaysConverter.isCheckDayOfWeek(weekDays, dayOfWeek))
            .boxed()
            .toList();
    }

    private static boolean isContainsDuplicatedWeekDay(DayOfWeek[] dayOfWeeks) {
        return dayOfWeeks.length != new HashSet<>(List.of(dayOfWeeks)).size();
    }

    public boolean isDateCheckDayOfWeek(LocalDate date) {
        return CheckDaysConverter.isCheckDayOfWeek(checkDays, date.getDayOfWeek());
    }

    public String toKorean() {
        return CheckDaysConverter.toKorean(checkDays);
    }

    // TODO: 2023/08/20 접근제한자 수정해서 캡슐화해야 하거나 외부로 분리
    @RequiredArgsConstructor
    public enum CheckDaysConverter {
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

        public static DayOfWeek[] toDayOfWeeks(String korWeekDays) {
            return Arrays.stream(korWeekDays.split(""))
                .map(day ->
                    Arrays.stream(CheckDaysConverter.values())
                        .filter(v -> day.equals(v.getKor()))
                        .map(Enum::name)
                        .collect(Collectors.joining())
                )
                .map(DayOfWeek::valueOf)
                .toArray(DayOfWeek[]::new);
        }

        static String toKorean(int weekDays) {
            return Arrays.stream(values())
                .filter(day -> isValueContainsDayOfWeek(weekDays, day.shift))
                .map(day -> day.kor)
                .collect(Collectors.joining());
        }

        static DayOfWeek[] toDayOfWeeks(int weekDays) {
            return Arrays.stream(values())
                .filter(day -> isValueContainsDayOfWeek(weekDays, day.shift))
                .map(day -> DayOfWeek.valueOf(day.name()))
                .toArray(DayOfWeek[]::new);
        }

        static int toValue(DayOfWeek[] dayOfWeeks) {
            int value = 0;
            for (DayOfWeek dayOfWeek : dayOfWeeks) {
                value |= (1 << CheckDaysConverter.valueOf(dayOfWeek.toString()).shift);
            }
            return value;
        }

        static boolean isCheckDayOfWeek(int weekDays, DayOfWeek dayOfWeek) {
            return isValueContainsDayOfWeek(weekDays, valueOf(dayOfWeek.toString()).shift);
        }

        private static boolean isValueContainsDayOfWeek(int weekDays, int date) {
            return (weekDays & (1 << date)) != 0;
        }
    }
}
