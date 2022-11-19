package checkmate.goal.domain;

import checkmate.common.util.WeekDayConverter;
import checkmate.exception.InvalidWeekDaysException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// TODO: 2022/07/22 이건 어떻게 처리할지 좀 더 고민 -> 후순위
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalCheckDays implements Serializable {
    @Column(name = "check_days", nullable = false)
    private int checkDays;

    public GoalCheckDays(String korWeekDays) {
        if (Pattern.compile("[^월화수목금토일]").matcher(korWeekDays).find())
            throw new InvalidWeekDaysException();
        // TODO: 2022/11/19 같은 문자가 두 개 이상 들어올 때 예외 처리 추가 ex) 월월
        this.checkDays = korWeekDaysToValue(korWeekDays);
    }

    public GoalCheckDays(int checkDays) {
        this.checkDays = checkDays;
    }

    public String getKorWeekDay() {
        return WeekDayConverter.valueToKorWeekDay(checkDays);
    }

    public int intValue() {
        return checkDays;
    }

    boolean isWorkingDay(LocalDate date) {
        return WeekDayConverter.isWorkingDay(checkDays, WeekDayConverter.localDateToValue(date));
    }

    int calcWorkingDayCount(Stream<LocalDate> dateStream) {
        return (int) dateStream.filter(this::isWorkingDay).count();
    }

    private int korWeekDaysToValue(String korWeekDays) {
        List<String> weekDayList = Arrays.stream(korWeekDays.split("")).toList();
        return weekDayList.stream()
                .mapToInt(weekDay -> WeekDayConverter.valueOf(WeekDayConverter.convertKorToEng(weekDay)).getValue())
                .sum();
    }

    @Override
    public String toString() {
        return String.valueOf(checkDays);
    }
}
