package checkmate.goal.application.dto;

import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import checkmate.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TeamMateCommandMapper {
    TeamMateCommandMapper INSTANCE = Mappers.getMapper(TeamMateCommandMapper.class);

    @Mappings({
            @Mapping(source = "inviter.id", target = "inviterUserId"),
            @Mapping(source = "inviter.nickname", target = "inviterNickname"),
            @Mapping(source = "invitee.id", target = "inviteeTeamMateId"),
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    TeamMateInviteNotificationDto toNotificationDto(User inviter, TeamMate invitee);

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
}
