package checkmate.user.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserSignUpCommand {
    private String providerId;
    private String username;
    private String email;
    private String nickname;

    @Builder
    public UserSignUpCommand(String providerId,
                             String username,
                             String email,
                             String nickname) {
        this.providerId = providerId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
    }
}
