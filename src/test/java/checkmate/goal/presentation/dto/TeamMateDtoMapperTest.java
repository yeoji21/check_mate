package checkmate.goal.presentation.dto;

import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMateDtoMapperTest {
    private static final TeamMateDtoMapper mapper = TeamMateDtoMapper.INSTANCE;

    @Test
    void teamMateInviteCommand() throws Exception{
        //given
        TeamMateInviteDto dto = new TeamMateInviteDto(1L, "nickname");
        long inviterUserId = 2L;

        //when
        TeamMateInviteCommand command = mapper.toCommand(dto, inviterUserId);

        //then
        isEqualTo(command.goalId(), dto.getGoalId());
        isEqualTo(command.inviteeNickname(), dto.getInviteeNickname());
        isEqualTo(command.inviterUserId(), inviterUserId);
    }

    @Test
    void teamMateInviteReplyCommand() throws Exception{
        //given
        TeamMateInviteReplyDto dto = new TeamMateInviteReplyDto(1L);
        long userId = 2L;

        //when
        TeamMateInviteReplyCommand command = mapper.toCommand(dto, userId);

        //then
        isEqualTo(command.notificationId(), dto.getNotificationId());
        isEqualTo(userId, command.userId());
    }

    private void isEqualTo(Object A, Object B) {
        assertThat(A).isEqualTo(B);
    }
}