package checkmate.goal.application.dto.request;

import lombok.Builder;

@Builder
public record TeamMateInviteCommand(
    long goalId,
    long inviterUserId,
    String inviteeNickname) {
}
