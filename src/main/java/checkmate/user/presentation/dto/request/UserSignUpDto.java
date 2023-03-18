package checkmate.user.presentation.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class UserSignUpDto {
    @NotBlank(message = "userIdentifier is blank")
    private String userIdentifier;

    @NotBlank(message = "username is blank")
    private String username;

    @Email(message = "invalid emailAddress form")
    private String emailAddress;

    @NotBlank
    @Size(max = 8)
    private String nickname;

    @Builder
    public UserSignUpDto(String userIdentifier,
                         String username,
                         String emailAddress,
                         String nickname) {
        this.userIdentifier = userIdentifier;
        this.username = username;
        this.emailAddress = emailAddress;
        this.nickname = nickname;
    }
}
