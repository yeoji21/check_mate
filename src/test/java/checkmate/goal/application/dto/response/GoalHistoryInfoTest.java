package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.GoalCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GoalHistoryInfoTest {

    @Test @DisplayName("객체 생성 테스트")
    void create() throws Exception{
        //given

        //when
        GoalHistoryInfo info = GoalHistoryInfo.builder()
                .id(1L)
                .title("title")
                .category(GoalCategory.ETC)
                .startDate(LocalDate.now().minusDays(9))
                .endDate(LocalDate.now().plusDays(10))
                .checkDays(CheckDaysConverter.toValue("월화수목금토일"))
                .appointmentTime(null)
                .workingDays(10)
                .teamMateNames(Collections.emptyList())
                .build();

        //then
        assertThat(info.getAchievementRate()).isEqualTo(50);
        assertThat(info.getCheckDays()).isEqualTo("월화수목금토일");
    }
}