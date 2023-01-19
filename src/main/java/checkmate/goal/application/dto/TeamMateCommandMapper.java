package checkmate.goal.application.dto;

import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.application.dto.response.TeamMateInviteReplyResult;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import checkmate.notification.domain.factory.dto.InviteReplyNotificationDto;
import checkmate.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamMateCommandMapper {
    TeamMateCommandMapper INSTANCE = Mappers.getMapper(TeamMateCommandMapper.class);

    TeamMateInviteReplyResult toInviteReplyResult(Long goalId);

    @Mappings({
            @Mapping(source = "sender.id", target = "inviterUserId"),
            @Mapping(source = "sender.nickname", target = "inviterNickname"),
            @Mapping(source = "inviteeTeamMate.id", target = "inviteeTeamMateId"),
            @Mapping(source = "inviteeTeamMate.userId", target = "inviteeUserId"),
            @Mapping(source = "inviteeTeamMate.goal.title", target = "goalTitle")
    })
    TeamMateInviteNotificationDto toInviteGoalNotificationDto(User sender, TeamMate inviteeTeamMate);

    @Mappings({
            @Mapping(source = "invitee.id", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.id", target = "goalId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    InviteReplyNotificationDto toInviteReplyNotificationDto(TeamMate invitee, String inviteeNickname, long inviterUserId, boolean accept);

    List<ExpulsionGoalNotificationDto> toKickOutNotificationDtos(List<TeamMate> teamMates);

    @Mappings({
            @Mapping(source = "teamMate.userId", target = "userId"),
            @Mapping(source = "teamMate.id", target = "teamMateId"),
            @Mapping(source = "teamMate.goal.title", target = "goalTitle")
    })
    ExpulsionGoalNotificationDto toKickOutNotificationDto(TeamMate teamMate);

    @Mappings({
            @Mapping(source = "teamMate.goal.id", target = "goalId"),
            @Mapping(source = "teamMate.id", target = "teamMateId")
    })
    TeamMateAcceptResult toResult(TeamMate teamMate);
}
