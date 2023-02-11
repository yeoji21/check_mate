package checkmate.goal.application.dto;

import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteAcceptNotificationDto;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TeamMateCommandMapper {
    TeamMateCommandMapper INSTANCE = Mappers.getMapper(TeamMateCommandMapper.class);

    @Mappings({
            @Mapping(source = "invitee.id", target = "inviteeTeamMateId"),
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    TeamMateInviteNotificationDto toNotificationDto(long inviterUserId, String inviterNickname, TeamMate invitee);

    @Mappings({
            @Mapping(source = "teamMate.userId", target = "userId"),
            @Mapping(source = "teamMate.id", target = "teamMateId"),
            @Mapping(source = "teamMate.goal.title", target = "goalTitle")
    })
    ExpulsionGoalNotificationDto toNotificationDto(TeamMate teamMate);

    @Mappings({
            @Mapping(source = "teamMate.goal.id", target = "goalId"),
            @Mapping(source = "teamMate.id", target = "teamMateId")
    })
    TeamMateAcceptResult toResult(TeamMate teamMate);

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
