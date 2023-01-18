package checkmate.goal.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InviteReplyDto {
    private long notificationId;

    public InviteReplyDto(long notificationId) {
        this.notificationId = notificationId;
    }
}
