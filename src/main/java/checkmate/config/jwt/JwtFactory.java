package checkmate.config.jwt;

import checkmate.config.auth.AuthConstants;
import checkmate.user.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFactory {
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${jwt.secret-key}")
    private String SECRET;
    @Value("${jwt.access-time}")
    private long ACCESS_EXPIRATION_TIME;
    @Value("${jwt.refresh-time}")
    private long REFRESH_EXPIRATION_TIME;

    public LoginToken createLoginToken(User user) {
        String refreshToken = refreshToken();
        // TODO: 2023/03/25 로그인 로직 통합 후 제거
        String key = user.getIdentifier() == null ? user.getProviderId() : user.getIdentifier();
        redisTemplate.opsForValue().set(key, refreshToken, 30, TimeUnit.DAYS);
        return LoginToken.builder()
                .accessToken(AuthConstants.TOKEN_PREFIX.getValue() + accessToken(user))
                .refreshToken(AuthConstants.TOKEN_PREFIX.getValue() + refreshToken)
                .build();
    }

    protected String accessToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
                .withClaim("id", user.getId())
                .withClaim("providerId", user.getProviderId())
                .withClaim("nickname", user.getNickname())
                .withClaim("auth", user.getRole())
                .sign(Algorithm.HMAC512(SECRET));
    }

    protected String refreshToken() {
        return JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SECRET));
    }
}
