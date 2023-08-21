package checkmate.goal.application.dto;

import checkmate.MapperTest;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class GoalCommandMapperTest extends MapperTest {

    private static final GoalCommandMapper mapper = GoalCommandMapper.INSTANCE;

    @Test
    void goal() throws Exception {
        //given
        GoalCreateCommand command = GoalCreateCommand.builder()
            .category(GoalCategory.ETC)
            .title("title")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(10))
            .checkDays(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY})
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
        isEqualTo(goal.getCheckDays().toKorean().split("").length, command.checkDays().length);
        isEqualTo(goal.getAppointmentTime(), command.appointmentTime());
    }
}