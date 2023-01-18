package checkmate.goal.application.dto.request;

import lombok.Builder;

@Builder
public record InviteReplyCommand(
        long userId,
        long notificationId) {
}
