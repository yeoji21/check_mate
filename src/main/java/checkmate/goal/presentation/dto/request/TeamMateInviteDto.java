package checkmate.goal.presentation.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class TeamMateInviteDto {
    @NotNull(message = "goalId is null")
    private long goalId;
    @NotBlank(message = "inviteeNickname is null")
    private String inviteeNickname;

    public TeamMateInviteDto(long goalId, String inviteeNickname) {
        this.goalId = goalId;
        this.inviteeNickname = inviteeNickname;
    }
}
