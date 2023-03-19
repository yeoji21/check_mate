package checkmate.user.application;

import checkmate.TestEntityFactory;
import checkmate.config.jwt.JwtDecoder;
import checkmate.config.jwt.JwtFactory;
import checkmate.config.jwt.LoginToken;
import checkmate.exception.NotFoundException;
import checkmate.user.application.dto.request.SnsLoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.presentation.dto.response.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private JwtFactory jwtFactory;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private LoginService loginService;
    private User user;

    @BeforeEach
    void setUp() {
        user = TestEntityFactory.user(1L, "tester");
        user.updateFcmToken("fcm token");
    }

    @Test
    void 로그인_성공_테스트() throws Exception {
        //given
        LoginToken loginToken = createLoginToken();
        given(userRepository.findByProviderId(any(String.class))).willReturn(Optional.of(user));
        given(jwtFactory.createLoginToken(any(User.class))).willReturn(loginToken);

        //when
        LoginResponse response = loginService.login(new SnsLoginCommand("id", "fcmToken"));

        //then
        assertThat(response).isInstanceOf(LoginResponse.class);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    private LoginToken createLoginToken() {
        return LoginToken.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();
    }

    @Test
    void 로그인_처음인_회원_테스트() throws Exception {
        //given
        given(userRepository.findByProviderId(any(String.class))).willReturn(Optional.empty());

        //when

        //then
        assertThrows(NotFoundException.class, () -> loginService.login(new SnsLoginCommand("id", "fcmToken")));
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissueToken() throws Exception {
        //given
        LoginToken loginToken = createLoginToken();
        TokenReissueCommand command = TokenReissueCommand.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        given(jwtDecoder.validateRefeshToken(command.accessToken(), command.refreshToken())).willReturn("providerId");
        given(userRepository.findByProviderId(any())).willReturn(Optional.ofNullable(user));
        given(jwtFactory.createLoginToken(any(User.class))).willReturn(loginToken);

        //when
        LoginResponse loginResponse = loginService.reissueToken(command);

        //then
        assertThat(loginResponse.accessToken()).isNotNull();
        assertThat(loginResponse.refreshToken()).isNotNull();
    }

    @Test
    void 로그아웃_테스트() throws Exception {
        //given
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));

        //when
        loginService.logout(user.getId());

        //then
        verify(redisTemplate).delete(user.getProviderId());
    }
}