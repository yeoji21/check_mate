package checkmate.mate.presentation.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MateInviteDto {

    @NotBlank(message = "inviteeNickname is null")
    private String inviteeNickname;
}
