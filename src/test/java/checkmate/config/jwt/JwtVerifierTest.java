package checkmate.config.jwt;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.domain.User;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtVerifierTest {
    @InjectMocks
    private JwtVerifier jwtVerifier;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void beforeEach() {
        jwtVerifier = new JwtVerifier(redisTemplate);
        ReflectionTestUtils.setField(jwtVerifier, "SECRET", "secret");
    }

    @Test
    @DisplayName("access token 검증")
    void vefify() throws Exception {
        //given
        String token = createAccessToken("secret");

        //when
        DecodedJWT decodedJWT = jwtVerifier.verify(token);

        //then
        assertThat(decodedJWT).isNotNull();
    }

    @Test
    @DisplayName("secret이 일치하지 않는 access token")
    void verify_fail() throws Exception {
        //given
        String token = createAccessToken("invalid secret");

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> jwtVerifier.verify(token));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_VERIFY_FAIL);
    }

    @Test
    @DisplayName("refresh token 검증")
    void verifyRefeshToken() throws Exception {
        //given
        String refreshToken = createRefreshToken("secret");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any())).willReturn("Bearer " + refreshToken);

        //when
        jwtVerifier.verifyRefeshToken("providerId", refreshToken);

        //then
        verify(valueOperations).get("providerId");
    }

    @Test
    @DisplayName("refresh token 검증 실패 - 토큰 불일치")
    void verifyRefeshToken_not_match() throws Exception {
        //given
        String refreshToken = createRefreshToken("secret");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any())).willReturn("Bearer invalid refresh token");

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> jwtVerifier.verifyRefeshToken("providerId", refreshToken));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("refresh token 검증 실패 - 토큰 만료")
    void verifyRefeshToken_expired() throws Exception {
        //given
        String refreshToken = createRefreshToken("secret");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any())).willReturn(null);

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> jwtVerifier.verifyRefeshToken("providerId", refreshToken));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("refresh token 만료")
    void expireRefreshToken() throws Exception {
        //given
        User user = TestEntityFactory.user(1L, "user");

        //when
        jwtVerifier.expireRefreshToken(user.getProviderId());

        //then
        verify(redisTemplate).delete(user.getProviderId());
    }

    private String createAccessToken(String secret) {
        JwtFactory jwtFactory = new JwtFactory(redisTemplate);
        ReflectionTestUtils.setField(jwtFactory, "SECRET", secret);
        return jwtFactory.accessToken(TestEntityFactory.user(1L, "tester"));
    }

    private String createRefreshToken(String secret) {
        JwtFactory jwtFactory = new JwtFactory(redisTemplate);
        ReflectionTestUtils.setField(jwtFactory, "SECRET", secret);
        return jwtFactory.refreshToken();
    }
}