package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// TODO: 2023/08/24 요일별 boolean 필드 두는 방식 고려
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays {

    @Column(name = "check_days", nullable = false)
    private int checkDays;

    private GoalCheckDays(DayOfWeek[] dayOfWeeks) {
        if (containsDuplicatedWeekDay(dayOfWeeks)) {
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

    private static boolean containsDuplicatedWeekDay(DayOfWeek[] dayOfWeeks) {
        return dayOfWeeks.length != new HashSet<>(List.of(dayOfWeeks)).size();
    }

    public boolean isCheckDay(LocalDate date) {
        return CheckDaysConverter.isValueContainsDayOfWeek(checkDays, date.getDayOfWeek());
    }

    int getValue() {
        return checkDays;
    }
}
