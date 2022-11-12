package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record InviteGoalNotificationDto (
    long inviterUserId,
    String inviterNickname,
    String goalTitle,
    long inviteeUserId,
    long inviteeTeamMateId) implements NotificationCreateDto{
}
