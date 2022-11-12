package checkmate.notification.domain.factory.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public record InviteReplyNotificationDto (
    long inviteeUserId,
    String inviteeNickname,
    long goalId,
    String goalTitle,
    long inviterUserId,
    boolean accept) implements NotificationCreateDto{
}
