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
public class NaverSignUpDto {
    @NotBlank(message = "providerId is blank")
    private String providerId;
    @NotBlank(message = "username is blank")
    private String username;
    @Email(message = "not emailAddress form")
    private String emailAddress;
    @NotBlank @Size(max=8)
    private String nickname;
    private String fcmToken;

    @Builder
    public NaverSignUpDto(String providerId,
                          String username,
                          String emailAddress,
                          String nickname,
                          String fcmToken) {
        this.providerId = providerId;
        this.username = username;
        this.emailAddress = emailAddress;
        this.nickname = nickname;
        this.fcmToken = fcmToken;
    }
}
