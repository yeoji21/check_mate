package checkmate.goal.domain.goal;

import checkmate.common.util.WeekDayConverter;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class WeekDayConverterTest {

    @Test
    void check() throws Exception{

        List<Integer> collect = IntStream.range(1, 1111112)
                .mapToObj(String::valueOf)
                .filter(n -> n.matches("[01]+"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        collect.forEach(System.out::println);
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