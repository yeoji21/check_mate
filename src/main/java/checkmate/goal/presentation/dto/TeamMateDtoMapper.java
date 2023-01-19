package checkmate.goal.presentation.dto;

import checkmate.goal.application.dto.request.InviteReplyCommand;
import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TeamMateDtoMapper {
    TeamMateDtoMapper INSTANCE = Mappers.getMapper(TeamMateDtoMapper.class);

    TeamMateInviteCommand toInviteCommand(TeamMateInviteDto inviteDto, long inviterUserId);

    @Mapping(target = "notificationId", source = "dto.notificationId")
    InviteReplyCommand toCommand(TeamMateInviteReplyDto dto, long userId);
}
