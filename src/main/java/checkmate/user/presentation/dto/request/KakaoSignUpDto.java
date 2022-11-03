package checkmate.user.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoSignUpDto {
    @NotBlank(message = "providerId is blank")
    private String providerId;
    @NotBlank(message = "username is blank")
    private String username;
    @Email(message = "not email form")
    private String email;
    @NotBlank @Size(max=8)
    private String nickname;
    private String fcmToken;

    @Builder
    public KakaoSignUpDto(String providerId,
                          String username,
                          String email,
                          String nickname,
                          String fcmToken) {
        this.providerId = providerId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.fcmToken = fcmToken;
    }
}
