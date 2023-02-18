package checkmate.config.jwt;

import checkmate.TestEntityFactory;
import checkmate.user.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFactoryTest {
    private static final long ACCESS_TIME = TimeUnit.MINUTES.toMillis(30);
    private static final long REFRESH_TIME = TimeUnit.DAYS.toMillis(30);
    private User user;
    private JwtFactory jwtFactory;

    @BeforeEach
    void setUp() {
        jwtFactory = new JwtFactory();
        ReflectionTestUtils.setField(jwtFactory, "SECRET", "secret");
        ReflectionTestUtils.setField(jwtFactory, "ACCESS_EXPIRATION_TIME", ACCESS_TIME);
        ReflectionTestUtils.setField(jwtFactory, "REFRESH_EXPIRATION_TIME", REFRESH_TIME);
        user = TestEntityFactory.user(11L, "tester");
    }

    @Test
    void AccessToken_생성_테스트() throws Exception {
        //given

        //when
        String accessToken = jwtFactory.accessToken(user);
        DecodedJWT decodedJWT = JWT.decode(accessToken);

        //then
        assertThat(accessToken).isNotNull();
        assertThat(decodedJWT.getClaim("id").asLong()).isEqualTo(11L);
        assertThat(decodedJWT.getClaim("nickname").asString()).isEqualTo("tester");
        assertThat(decodedJWT.getExpiresAt()).isBefore(new Date(System.currentTimeMillis() + ACCESS_TIME));
    }

    @Test
    void RefreshToken_생성_테스트() throws Exception {
        //given

        //when
        String refreshToken = jwtFactory.refreshToken();
        DecodedJWT decodedJWT = JWT.decode(refreshToken);

        //then
        assertThat(refreshToken).isNotNull();
        assertThat(decodedJWT.getExpiresAt()).isAfter(new Date(System.currentTimeMillis() + ACCESS_TIME));
        assertThat(decodedJWT.getExpiresAt()).isBefore(new Date(System.currentTimeMillis() + REFRESH_TIME));
    }

    @Test
    void token() throws Exception {
        User user = TestEntityFactory.user(1L, "hi");
        ReflectionTestUtils.setField(user, "providerId", "testId");
        ReflectionTestUtils.setField(user, "username", "tester");
        String made = jwtFactory.accessToken(user);
        System.out.println(made);

        String sign = JWT.create()
                .withSubject(this.user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TIME))
                .withClaim("id", 1L)
                .withClaim("providerId", "user")
                .withClaim("nickname", "user")
                .withClaim("auth", this.user.getRole())
                .sign(Algorithm.HMAC512("test"));
        System.out.println(sign);
    }
}