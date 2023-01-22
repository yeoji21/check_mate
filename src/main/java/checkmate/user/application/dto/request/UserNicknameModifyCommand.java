package checkmate.user.application.dto.request;

import lombok.Builder;

@Builder
public record UserNicknameModifyCommand (
    long userId,
    String nickname ){
}
