package checkmate.mate.presentation.dto;

import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MateDtoMapper {
    MateDtoMapper INSTANCE = Mappers.getMapper(MateDtoMapper.class);

    TeamMateInviteCommand toCommand(MateInviteDto inviteDto, long inviterUserId);

    @Mapping(target = "notificationId", source = "dto.notificationId")
    TeamMateInviteReplyCommand toCommand(MateInviteReplyDto dto, long userId);
}
