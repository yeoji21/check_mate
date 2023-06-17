package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import com.mysema.commons.lang.Assert;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays {

    @Column(name = "check_days", nullable = false)
    private int checkDays;

    private GoalCheckDays(String korWeekDays) {
        validateKorWeekDay(korWeekDays);
        this.checkDays = CheckDaysConverter.toValue(korWeekDays);
    }

    public GoalCheckDays(int value) {
        this(CheckDaysConverter.toKorWeekDays(value));
    }

    public static GoalCheckDays ofKorean(String korWeekDays) {
        return new GoalCheckDays(korWeekDays);
    }

    public static GoalCheckDays ofValue(int value) {
        return new GoalCheckDays(CheckDaysConverter.toKorWeekDays(value));
    }

    public static GoalCheckDays ofLocalDates(LocalDate... dates) {
        return new GoalCheckDays(Arrays.stream(dates)
            .map(CheckDaysConverter::toKoreanWeekDay)
            .collect(Collectors.joining()));
    }

    private static void validateKorWeekDay(String korWeekDays) {
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

    public int toInt() {
        return checkDays;
    }

    public String toKorean() {
        return CheckDaysConverter.toKorWeekDays(checkDays);
    }

    int getWorkingDayCount(Stream<LocalDate> dateStream) {
        return (int) dateStream.filter(this::isWorkingDay).count();
    }

    boolean isWorkingDay(LocalDate date) {
        return CheckDaysConverter.isWorkingDay(checkDays, date);
    }

    @Override
    public String toString() {
        return String.valueOf(checkDays);
    }
}
