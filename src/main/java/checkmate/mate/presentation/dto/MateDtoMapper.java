package checkmate.mate.presentation.dto;

import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MateDtoMapper {

    MateDtoMapper INSTANCE = Mappers.getMapper(MateDtoMapper.class);

    MateInviteCommand toCommand(long goalId, long inviterUserId, MateInviteDto inviteDto);

    @Mapping(target = "notificationId", source = "dto.notificationId")
    MateInviteReplyCommand toCommand(MateInviteReplyDto dto, long userId);
}
