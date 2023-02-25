package checkmate.mate.application.dto.request;

import lombok.Builder;

@Builder
public record MateInviteReplyCommand(
        long userId,
        long notificationId) {
}
