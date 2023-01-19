package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record TeamMateInviteNotificationDto(
    long inviterUserId,
    String inviterNickname,
    String goalTitle,
    long inviteeUserId,
    long inviteeTeamMateId) implements NotificationCreateDto{
    @Override
    public long getSenderUserId() {
        return inviterUserId;
    }
}
