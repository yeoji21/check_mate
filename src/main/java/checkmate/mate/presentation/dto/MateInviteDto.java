package checkmate.mate.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class MateInviteDto {
    @NotBlank(message = "inviteeNickname is null")
    private String inviteeNickname;

    public MateInviteDto(String inviteeNickname) {
        this.inviteeNickname = inviteeNickname;
    }
}
