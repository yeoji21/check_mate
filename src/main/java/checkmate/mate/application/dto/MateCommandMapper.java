package checkmate.mate.application.dto;

import checkmate.goal.domain.TeamMate;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteAcceptNotificationDto;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.MateInviteNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MateCommandMapper {
    MateCommandMapper INSTANCE = Mappers.getMapper(MateCommandMapper.class);

    @Mappings({
            @Mapping(source = "invitee.id", target = "inviteeMateId"),
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    MateInviteNotificationDto toNotificationDto(long inviterUserId, String inviterNickname, TeamMate invitee);

    @Mappings({
            @Mapping(source = "teamMate.userId", target = "userId"),
            @Mapping(source = "teamMate.id", target = "mateId"),
            @Mapping(source = "teamMate.goal.title", target = "goalTitle")
    })
    ExpulsionGoalNotificationDto toNotificationDto(TeamMate teamMate);

    @Mappings({
            @Mapping(source = "teamMate.goal.id", target = "goalId"),
            @Mapping(source = "teamMate.id", target = "mateId")
    })
    MateAcceptResult toResult(TeamMate teamMate);

    @Mappings({
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.id", target = "goalId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    InviteAcceptNotificationDto toAcceptNotificationDto(TeamMate invitee, String inviteeNickname, long inviterUserId);

    @Mappings({
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.id", target = "goalId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    InviteRejectNotificationDto toRejectNotificationDto(TeamMate invitee, String inviteeNickname, long inviterUserId);
}
