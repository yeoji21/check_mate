package checkmate.goal.presentation.dto;

import checkmate.MapperTest;
import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import checkmate.mate.presentation.dto.MateDtoMapper;
import checkmate.mate.presentation.dto.MateInviteDto;
import checkmate.mate.presentation.dto.MateInviteReplyDto;
import org.junit.jupiter.api.Test;

class MateDtoMapperTest extends MapperTest {
    private static final MateDtoMapper mapper = MateDtoMapper.INSTANCE;

    @Test
    void teamMateInviteCommand() throws Exception {
        //given
        MateInviteDto dto = new MateInviteDto(1L, "nickname");
        long inviterUserId = 2L;

        //when
        MateInviteCommand command = mapper.toCommand(dto, inviterUserId);

        //then
        isEqualTo(command.goalId(), dto.getGoalId());
        isEqualTo(command.inviteeNickname(), dto.getInviteeNickname());
        isEqualTo(command.inviterUserId(), inviterUserId);
    }

    @Test
    void teamMateInviteReplyCommand() throws Exception {
        //given
        MateInviteReplyDto dto = new MateInviteReplyDto(1L);
        long userId = 2L;

        //when
        MateInviteReplyCommand command = mapper.toCommand(dto, userId);

        //then
        isEqualTo(command.notificationId(), dto.getNotificationId());
        isEqualTo(userId, command.userId());
    }
}