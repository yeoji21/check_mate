package checkmate.goal.presentation.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamMateInviteReplyDto {
    private long teamMateId;
    private long notificationId;
    private boolean accept;

    @Builder
    public TeamMateInviteReplyDto(long teamMateId, long notificationId, boolean accept) {
        this.teamMateId = teamMateId;
        this.notificationId = notificationId;
        this.accept = accept;
    }
}
