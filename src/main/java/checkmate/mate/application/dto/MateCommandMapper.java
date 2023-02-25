package checkmate.mate.application.dto;

import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.domain.Mate;
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
    MateInviteNotificationDto toNotificationDto(long inviterUserId, String inviterNickname, Mate invitee);

    @Mappings({
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "id", target = "mateId"),
            @Mapping(source = "goal.title", target = "goalTitle")
    })
    ExpulsionGoalNotificationDto toNotificationDto(Mate mate);

    @Mappings({
            @Mapping(source = "goal.id", target = "goalId"),
            @Mapping(source = "id", target = "mateId")
    })
    MateAcceptResult toResult(Mate mate);

    @Mappings({
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.id", target = "goalId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    InviteAcceptNotificationDto toAcceptNotificationDto(Mate invitee, String inviteeNickname, long inviterUserId);

    @Mappings({
            @Mapping(source = "invitee.userId", target = "inviteeUserId"),
            @Mapping(source = "invitee.goal.id", target = "goalId"),
            @Mapping(source = "invitee.goal.title", target = "goalTitle")
    })
    InviteRejectNotificationDto toRejectNotificationDto(Mate invitee, String inviteeNickname, long inviterUserId);
}
