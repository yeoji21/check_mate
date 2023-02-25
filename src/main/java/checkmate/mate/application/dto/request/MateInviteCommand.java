package checkmate.mate.application.dto.request;

import lombok.Builder;

@Builder
public record MateInviteCommand(
        long goalId,
        long inviterUserId,
        String inviteeNickname) {
}
