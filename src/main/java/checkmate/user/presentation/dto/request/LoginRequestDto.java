package checkmate.user.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LoginRequestDto {
    @NotBlank(message = "identifier is blank")
    private String identifier;
    private String fcmToken;

    @Builder
    public LoginRequestDto(String identifier, String fcmToken) {
        this.identifier = identifier;
        this.fcmToken = fcmToken;
    }
}
