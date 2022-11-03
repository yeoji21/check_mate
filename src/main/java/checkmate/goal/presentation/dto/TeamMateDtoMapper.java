package checkmate.goal.presentation.dto;

import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TeamMateDtoMapper {
    TeamMateDtoMapper INSTANCE = Mappers.getMapper(TeamMateDtoMapper.class);

    TeamMateInviteCommand toInviteCommand(TeamMateInviteDto inviteDto, long inviterUserId);

    TeamMateInviteReplyCommand toInviteReplyCommand(TeamMateInviteReplyDto dto);
}
