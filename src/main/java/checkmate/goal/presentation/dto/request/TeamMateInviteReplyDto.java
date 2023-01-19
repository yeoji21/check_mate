package checkmate.goal.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamMateInviteReplyDto {
    private long notificationId;

    public TeamMateInviteReplyDto(long notificationId) {
        this.notificationId = notificationId;
    }
}
