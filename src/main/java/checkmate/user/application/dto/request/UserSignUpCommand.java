package checkmate.user.application.dto.request;

import lombok.Builder;

@Builder
public record UserSignUpCommand(
        String identifier,
        String username,
        String emailAddress,
        String nickname) {
}
