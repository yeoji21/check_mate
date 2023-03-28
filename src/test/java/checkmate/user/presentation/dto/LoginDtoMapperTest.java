package checkmate.user.presentation.dto;

import checkmate.MapperTest;
import checkmate.user.application.dto.request.LoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.presentation.dto.request.LoginRequestDto;
import checkmate.user.presentation.dto.request.TokenReissueDto;
import org.junit.jupiter.api.Test;

class LoginDtoMapperTest extends MapperTest {
    private static final LoginDtoMapper mapper = LoginDtoMapper.INSTANCE;

    @Test
    void loginRequestDto() throws Exception {
        //given
        LoginRequestDto dto = LoginRequestDto.builder()
                .fcmToken("fcmToken")
                .identifier("identifier")
                .build();

        //when
        LoginCommand command = mapper.toCommand(dto);

        //then
        isEqualTo(command.fcmToken(), dto.getFcmToken());
        isEqualTo(command.identifier(), dto.getIdentifier());
    }

    @Test
    void tokenReissueCommand() throws Exception {
        //given
        TokenReissueDto dto = TokenReissueDto.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        //when
        TokenReissueCommand command = mapper.toCommand(dto);

        //then
        isEqualTo(command.accessToken(), dto.getAccessToken());
        isEqualTo(command.refreshToken(), dto.getRefreshToken());
    }
}