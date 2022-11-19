package checkmate.goal.domain.goal;

import checkmate.common.util.WeekDayConverter;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeekDayConverterTest {

    @Test
    void error() throws Exception{
        //given
        String korWeekDays = "월월";

        //when
        List<String> weekDayList = Arrays.stream(korWeekDays.split("")).toList();
        int value = weekDayList.stream()
                .mapToInt(weekDay -> WeekDayConverter.valueOf(WeekDayConverter.convertKorToEng(weekDay)).getValue())
                .sum();

        //then
        System.out.println(value);
    }

    @Test
    void 값에서_요일로_변경() throws Exception{
        //given

        //when

        //then
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        assertThat(WeekDayConverter.valueToKorWeekDay(1010100)).isEqualTo("월수금");
        assertThat(WeekDayConverter.valueToKorWeekDay(1111111)).isEqualTo("월화수목금토일");
        assertThat(WeekDayConverter.valueToKorWeekDay(11)).isEqualTo("토일");
        assertThat(WeekDayConverter.valueToKorWeekDay(101010)).isEqualTo("화목토");
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}