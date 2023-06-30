package checkmate.mate.presentation.dto;

import checkmate.MapperTest;
import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import org.junit.jupiter.api.Test;

class MateDtoMapperTest extends MapperTest {

    private static final MateDtoMapper mapper = MateDtoMapper.INSTANCE;

    @Test
    void toMateInviteCommand() throws Exception {
        //given
        MateInviteDto dto = new MateInviteDto("nickname");
        long goalId = 1L;
        long inviterUserId = 2L;

        //when
        MateInviteCommand command = mapper.toCommand(goalId, dto, inviterUserId);

        //then
        isEqualTo(command.goalId(), goalId);
        isEqualTo(command.inviteeNickname(), dto.getInviteeNickname());
        isEqualTo(command.inviterUserId(), inviterUserId);
    }

    @Test
    void toMateInviteReplyCommand() throws Exception {
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