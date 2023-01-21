package checkmate.goal.application.dto.request;

import lombok.Builder;

@Builder
public record TeamMateInviteReplyCommand(
        long userId,
        long notificationId) {
}
