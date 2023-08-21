package checkmate.goal.presentation.dto;

import checkmate.MapperTest;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.Goal.GoalCategory;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class GoalDtoMapperTest extends MapperTest {

    private static final GoalDtoMapper mapper = GoalDtoMapper.INSTANCE;

    @Test
    void goalCreateCommand() throws Exception {
        //given
        GoalCreateDto dto = GoalCreateDto.builder()
            .category(GoalCategory.ETC)
            .title("title")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(10))
            .checkDays("월수금")
            .appointmentTime(LocalTime.now())
            .build();

        //when
        GoalCreateCommand command = mapper.toCommand(dto, 1L);

        //then
        isEqualTo(command.userId(), 1L);
        isEqualTo(command.category(), dto.getCategory());
        isEqualTo(command.title(), dto.getTitle());
        isEqualTo(command.startDate(), dto.getStartDate());
        isEqualTo(command.endDate(), dto.getEndDate());
        isEqualTo(command.checkDays().length, dto.getCheckDays().split("").length);
        isEqualTo(command.appointmentTime(), dto.getAppointmentTime());
    }

    @Test
    void goalModifyCommand() throws Exception {
        //given
        GoalModifyDto dto = GoalModifyDto.builder()
            .endDate(LocalDate.now().plusDays(10))
            .appointmentTime(LocalTime.now())
            .timeReset(true)
            .build();
        long goalId = 1L;
        long userId = 2L;

        //when
        GoalModifyCommand command = mapper.toCommand(dto, goalId, userId);

        //then
        isEqualTo(command.goalId(), goalId);
        isEqualTo(command.userId(), userId);
        isEqualTo(command.endDate(), dto.getEndDate());
        isEqualTo(command.appointmentTime(), dto.getAppointmentTime());
        isEqualTo(command.timeReset(), dto.isTimeReset());
    }

    @Test
    void likeCountCreateCommand() throws Exception {
        //given
        long goalId = 1L;
        long userId = 2L;
        LikeCountCreateDto dto = new LikeCountCreateDto(10);

        //when
        LikeCountCreateCommand command = mapper.toCommand(goalId, dto, userId);

        //then
        isEqualTo(command.goalId(), goalId);
        isEqualTo(command.likeCount(), dto.getLikeCount());
        isEqualTo(command.userId(), userId);
    }
}