package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record MateInviteNotificationDto(
        long inviterUserId,
        String inviterNickname,
        String goalTitle,
        long inviteeUserId,
        long inviteeMateId) implements NotificationCreateDto {
    @Override
    public long getSenderUserId() {
        return inviterUserId;
    }
}
