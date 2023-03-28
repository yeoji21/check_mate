package checkmate.user.application;

import checkmate.TestEntityFactory;
import checkmate.config.jwt.JwtFactory;
import checkmate.config.jwt.JwtVerifier;
import checkmate.config.jwt.LoginToken;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.application.dto.request.LoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.presentation.dto.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private JwtFactory jwtFactory;
    @Mock
    private JwtVerifier jwtVerifier;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("로그인 성공")
    void login() throws Exception {
        //given
        User user = createUser();
        LoginToken loginToken = createLoginToken();
        LoginCommand command = createLoginCommand(user);
        given(userRepository.findByIdentifier(any(String.class))).willReturn(Optional.of(user));
        given(jwtFactory.createLoginToken(any(User.class))).willReturn(loginToken);

        //when
        LoginResponse response = loginService.login(command);

        //then
        assertThat(response.accessToken()).startsWith("Bearer ");
        assertThat(response.refreshToken()).startsWith("Bearer ");
    }

    @Test
    @DisplayName("로그인 실패 - 회원이 아닌 경우")
    void login_not_exist() throws Exception {
        //given
        User user = createUser();
        LoginCommand command = createLoginCommand(user);
        given(userRepository.findByIdentifier(any(String.class))).willReturn(Optional.empty());

        //when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> loginService.login(command));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissueToken() throws Exception {
        //given
        User user = createUser();
        TokenReissueCommand command = createTokenReissueCommand();

        given(jwtVerifier.parseIdentifier(anyString())).willReturn(user.getIdentifier());
        given(userRepository.findByIdentifier(any())).willReturn(Optional.ofNullable(user));
        given(jwtFactory.createLoginToken(any(User.class))).willReturn(createLoginToken());

        //when
        LoginResponse response = loginService.reissueToken(command);

        //then
        verify(jwtVerifier).verifyRefeshToken(user.getIdentifier(), command.refreshToken());
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        //given
        User user = createUser();
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));

        //when
        loginService.logout(user.getId());

        //then
        verify(jwtVerifier).expireRefreshToken(user.getIdentifier());
    }

    private TokenReissueCommand createTokenReissueCommand() {
        return TokenReissueCommand.builder()
                .accessToken("Bearer accessToken")
                .refreshToken("Bearer refreshToken")
                .build();
    }

    private User createUser() {
        return TestEntityFactory.user(1L, "tester");
    }

    private LoginCommand createLoginCommand(User user) {
        return new LoginCommand(user.getIdentifier(), user.getFcmToken());
    }

    private LoginToken createLoginToken() {
        return LoginToken.builder()
                .accessToken("Bearer accessToken")
                .refreshToken("Bearer refreshToken")
                .build();
    }
}