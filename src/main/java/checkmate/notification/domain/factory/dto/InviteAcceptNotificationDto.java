package checkmate.notification.domain.factory.dto;

public record InviteAcceptNotificationDto(
        long inviteeUserId,
        String inviteeNickname,
        long goalId,
        String goalTitle,
        long inviterUserId) implements NotificationCreateDto {
    @Override
    public long getSenderUserId() {
        return inviteeUserId;
    }
}
