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
public class WeekDays implements Serializable {
    @Column(name = "week_days", nullable = false)
    private int weekDays;

    public WeekDays(String korWeekDays) {
        if (Pattern.compile("[^월화수목금토일]").matcher(korWeekDays).find())
            throw new InvalidWeekDaysException();
        this.weekDays = korWeekDaysToValue(korWeekDays);
    }

    public WeekDays(int weekDays) {
        this.weekDays = weekDays;
    }

    public String getKorWeekDay() {
        return WeekDayConverter.valueToKorWeekDay(weekDays);
    }

    public int getIntValue() {
        return weekDays;
    }

    boolean isWorkingDay(LocalDate date) {
        return WeekDayConverter.isWorkingDay(weekDays, WeekDayConverter.localDateToValue(date));
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
}
