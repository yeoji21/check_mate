package checkmate.user.presentation.dto;

import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.presentation.dto.request.TokenReissueDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAssembler {
    public static TokenReissueCommand tokenReissueCommand(TokenReissueDto tokenReissueDto) {
        return TokenReissueCommand.builder()
                .refreshToken(tokenReissueDto.getRefreshToken())
                .accessToken(tokenReissueDto.getAccessToken())
                .build();
    }
}
