package checkmate.goal.domain.goal;

import checkmate.goal.domain.GoalCheckDays;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class GoalCheckDaysTest {
    @Test
    void regex() throws Exception{
        assertThat(getMatcher("월하").find()).isTrue();
        assertThat(getMatcher("월화수목금").find()).isFalse();
        assertThat(getMatcher("월화스스목금토일").find()).isTrue();
        assertThat(getMatcher("토일").find()).isFalse();
        assertThat(getMatcher("일").find()).isFalse();
        assertThat(getMatcher("월수금").find()).isFalse();
    }

    @Test
    void create() throws Exception{
        assertThat(new GoalCheckDays("월수금").intValue()).isEqualTo(1010100);
        assertThat(new GoalCheckDays("월화수목금토일").intValue()).isEqualTo(1111111);
        assertThat(new GoalCheckDays("토일").intValue()).isEqualTo(11);
        assertThat(new GoalCheckDays("화목토").intValue()).isEqualTo(101010);
    }

    private Matcher getMatcher(String days) {
        String regex = "[^월화수목금토일]";
        return Pattern.compile(regex).matcher(days);
    }
}