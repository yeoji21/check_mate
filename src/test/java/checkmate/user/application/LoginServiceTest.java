package checkmate.user.application;

import checkmate.TestEntityFactory;
import checkmate.config.jwt.JwtDecoder;
import checkmate.config.jwt.JwtFactory;
import checkmate.exception.format.BusinessException;
import checkmate.exception.format.NotFoundException;
import checkmate.user.application.dto.request.SnsLoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.domain.UserRole;
import checkmate.user.presentation.dto.response.LoginTokenResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock private JwtFactory jwtFactory;
    @Mock private JwtDecoder jwtDecoder;
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @InjectMocks private LoginService loginService;
    private User user;

    @BeforeEach
    void setUp() {
        user = TestEntityFactory.user(1L, "tester");
        user.updateFcmToken("fcm token");
    }

    @Test
    void 로그인_성공_테스트() throws Exception{
        //given
        given(userRepository.findByProviderId(any(String.class))).willReturn(Optional.of(user));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        //when
        LoginTokenResponse response = loginService.login(new SnsLoginCommand("id", "fcmToken"));

        //then
        assertThat(response).isInstanceOf(LoginTokenResponse.class);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void 로그인_처음인_회원_테스트() throws Exception{
        //given
        given(userRepository.findByProviderId(any(String.class))).willReturn(Optional.empty());

        //when

        //then
        assertThrows(NotFoundException.class, () -> loginService.login(new SnsLoginCommand("id", "fcmToken")));
    }

    @Test
    void 닉네임을_설정하지_않았던_회원_테스트() throws Exception{
        //given
        User user = User.builder()
                .emailAddress("mail")
                .providerId("id")
                .username("name")
                .fcmToken("fcm token")
                .role(UserRole.USER.getRole())
                .build();

        given(userRepository.findByProviderId(any(String.class))).willReturn(Optional.of(user));

        //when

        //then
        assertThrows(BusinessException.class, () -> loginService.login(new SnsLoginCommand("id", "fcm token")));
    }

    @Test @DisplayName("토큰 재발급 테스트")
    void reissueToken() throws Exception{
        //given
        TokenReissueCommand requestDto = TokenReissueCommand.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any())).willReturn("Bearer refreshToken");
        given(jwtDecoder.getProviderId(anyString())).willReturn("1");
        given(userRepository.findByProviderId(any())).willReturn(Optional.ofNullable(user));

        //when
        LoginTokenResponse loginTokenResponse = loginService.reissueToken(requestDto);

        //then
        assertThat(loginTokenResponse.getAccessToken()).isNotNull();
        assertThat(loginTokenResponse.getRefreshToken()).isNotNull();
    }

    @Test
    void 로그아웃_테스트() throws Exception{
        //given
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));

        //when
        loginService.logout(user.getId());

        //then
        verify(redisTemplate).delete(user.getProviderId());
    }
}