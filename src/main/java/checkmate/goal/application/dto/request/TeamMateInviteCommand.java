package checkmate.goal.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class TeamMateInviteCommand {
    private long goalId;
    private long inviterUserId;
    private String inviteeNickname;
}
