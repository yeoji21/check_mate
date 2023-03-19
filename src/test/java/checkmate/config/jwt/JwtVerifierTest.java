package checkmate.config.jwt;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtVerifierTest {
    private JwtVerifier jwtVerifier;
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void beforeEach() {
        jwtVerifier = new JwtVerifier(redisTemplate);
        ReflectionTestUtils.setField(jwtVerifier, "SECRET", "secret");
    }

    @Test
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
    void vefify() throws Exception {
        //given
        String token = createAccessToken("secret");

        //when
        DecodedJWT decodedJWT = jwtVerifier.verify(token);

        //then
        assertThat(decodedJWT).isNotNull();
    }

    private String createAccessToken(String secret) {
        JwtFactory jwtFactory = new JwtFactory(redisTemplate);
        ReflectionTestUtils.setField(jwtFactory, "SECRET", secret);
        return jwtFactory.accessToken(TestEntityFactory.user(1L, "tester"));
    }
}