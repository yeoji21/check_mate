package checkmate.goal.domain;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import com.mysema.commons.lang.Assert;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays implements Serializable {

    @Column(name = "check_days", nullable = false)
    private int checkDays;

    public GoalCheckDays(String korWeekDays) {
        correctDayCheck(korWeekDays);
        duplicateDayCheck(korWeekDays);
        this.checkDays = CheckDaysConverter.toValue(korWeekDays);
    }

    public GoalCheckDays(int value) {
        this(CheckDaysConverter.toKorWeekDays(value));
    }

    public int intValue() {
        return checkDays;
    }

    boolean isWorkingDay(LocalDate date) {
        return CheckDaysConverter.isWorkingDay(checkDays, date);
    }

    int calcWorkingDayCount(Stream<LocalDate> dateStream) {
        return (int) dateStream.filter(this::isWorkingDay).count();
    }

    private void correctDayCheck(String korWeekDays) {
        if (Pattern.compile("[^월화수목금토일]").matcher(korWeekDays).find()) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_DAYS);
        }
    }

    private void duplicateDayCheck(String korWeekDays) {
        String[] split = korWeekDays.split("");
        Assert.isTrue(split.length == new HashSet<>(List.of(split)).size(), "중복 요일");
    }

    @Override
    public String toString() {
        return String.valueOf(checkDays);
    }
}
