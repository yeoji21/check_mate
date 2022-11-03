package checkmate.goal.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TeamMateInviteReplyCommand {
    private long teamMateId;
    private long notificationId;
    private boolean accept;
}
