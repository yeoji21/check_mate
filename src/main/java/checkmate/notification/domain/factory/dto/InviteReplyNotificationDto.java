package checkmate.notification.domain.factory.dto;

import lombok.Builder;

@Builder
public record InviteReplyNotificationDto (
    long inviteeUserId,
    String inviteeNickname,
    long goalId,
    String goalTitle,
    long inviterUserId,
    boolean accept) implements NotificationCreateDto{
    @Override
    public long getSenderUserId() {
        return inviteeUserId;
    }
}
