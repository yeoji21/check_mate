package checkmate.goal.application.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class GoalCommandMapperTest {
    private static final GoalCommandMapper mapper = GoalCommandMapper.INSTANCE;

    @Test
    void goal() throws Exception{
        //given
        GoalCreateCommand command = GoalCreateCommand.builder()
                .category(GoalCategory.ETC)
                .title("title")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .checkDays("월수금")
                .appointmentTime(LocalTime.now())
                .userId(1L)
                .build();

        //when
        Goal goal = mapper.toEntity(command);

        //then
        isEqualTo(goal.getCategory(), command.category());
        isEqualTo(goal.getTitle(), command.title());
        isEqualTo(goal.getStartDate(), command.startDate());
        isEqualTo(goal.getEndDate(), command.endDate());
        isEqualTo(CheckDaysConverter.toDays(goal.getCheckDays().intValue()), command.checkDays());
        isEqualTo(goal.getAppointmentTime(), command.appointmentTime());
    }

    private void isEqualTo(Object A, Object B) {
        assertThat(A).isEqualTo(B);
    }
}