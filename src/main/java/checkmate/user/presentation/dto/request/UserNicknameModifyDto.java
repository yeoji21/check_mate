package checkmate.user.presentation.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNicknameModifyDto {
    @NotBlank @Size(max=8)
    private String nickname;

    public UserNicknameModifyDto(String nickname) {
        this.nickname = nickname;
    }
}
