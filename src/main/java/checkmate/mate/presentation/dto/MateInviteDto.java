package checkmate.mate.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class MateInviteDto {
    @NotNull(message = "goalId is null")
    private long goalId;
    @NotBlank(message = "inviteeNickname is null")
    private String inviteeNickname;

    public MateInviteDto(long goalId, String inviteeNickname) {
        this.goalId = goalId;
        this.inviteeNickname = inviteeNickname;
    }
}
