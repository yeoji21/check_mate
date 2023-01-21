package checkmate.goal.presentation.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.goal.presentation.dto.request.LikeCountCreateDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class GoalDtoMapperTest {
    private static final GoalDtoMapper mapper = GoalDtoMapper.INSTANCE;

    @Test
    void goalCreateCommand() throws Exception{
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
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.category()).isEqualTo(dto.getCategory());
        assertThat(command.title()).isEqualTo(dto.getTitle());
        assertThat(command.startDate()).isEqualTo(dto.getStartDate());
        assertThat(command.endDate()).isEqualTo(dto.getEndDate());
        assertThat(command.checkDays()).isEqualTo(dto.getCheckDays());
        assertThat(command.appointmentTime()).isEqualTo(dto.getAppointmentTime());
    }

    @Test
    void goalModifyCommand() throws Exception{
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
        assertThat(command.goalId()).isEqualTo(goalId);
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.endDate()).isEqualTo(dto.getEndDate());
        assertThat(command.appointmentTime()).isEqualTo(dto.getAppointmentTime());
        assertThat(command.timeReset()).isEqualTo(dto.isTimeReset());
    }

    @Test
    void likeCountCreateCommand() throws Exception{
        //given
        LikeCountCreateDto dto = LikeCountCreateDto.builder()
                .goalId(1L)
                .likeCount(10)
                .build();
        long userId = 2L;

        //when
        LikeCountCreateCommand command = mapper.toCommand(dto, userId);

        //then
        assertThat(command.goalId()).isEqualTo(dto.getGoalId());
        assertThat(command.likeCount()).isEqualTo(dto.getLikeCount());
        assertThat(command.userId()).isEqualTo(userId);
    }
}