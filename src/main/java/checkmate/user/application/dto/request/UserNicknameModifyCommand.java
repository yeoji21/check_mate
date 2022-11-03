package checkmate.user.application.dto.request;

import lombok.Getter;

@Getter
public class UserNicknameModifyCommand {
    private Long userId;
    private String nickname;

    public UserNicknameModifyCommand(long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
