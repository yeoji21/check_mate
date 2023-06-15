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

    // TODO: 2023/06/15 정적 팩토리 메소드로 변경 고려
    public GoalCheckDays(String korWeekDays) {
        validateKorWeekDay(korWeekDays);
        this.checkDays = CheckDaysConverter.toValue(korWeekDays);
    }

    public GoalCheckDays(int value) {
        this(CheckDaysConverter.toKorWeekDays(value));
    }

    public int intValue() {
        return checkDays;
    }

    int calcWorkingDayCount(Stream<LocalDate> dateStream) {
        return (int) dateStream.filter(this::isWorkingDay).count();
    }

    boolean isWorkingDay(LocalDate date) {
        return CheckDaysConverter.isWorkingDay(checkDays, date);
    }

    private void validateKorWeekDay(String korWeekDays) {
        if (Pattern.compile("[^월화수목금토일]").matcher(korWeekDays).find()) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_DAYS);
        }
        checkDuplicateWeekDay(korWeekDays);
    }

    private void checkDuplicateWeekDay(String korWeekDays) {
        Assert.isTrue(
            korWeekDays.length() == new HashSet<>(List.of(korWeekDays.split(""))).size(),
            "중복 요일");
    }

    @Override
    public String toString() {
        return String.valueOf(checkDays);
    }
}
