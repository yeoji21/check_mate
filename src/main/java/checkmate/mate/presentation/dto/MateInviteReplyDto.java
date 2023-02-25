package checkmate.mate.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MateInviteReplyDto {
    private long notificationId;

    public MateInviteReplyDto(long notificationId) {
        this.notificationId = notificationId;
    }
}
