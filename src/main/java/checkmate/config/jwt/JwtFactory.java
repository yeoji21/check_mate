package checkmate.config.jwt;

import checkmate.user.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtFactory {
    @Value("${jwt.secret-key}")
    private String SECRET;
    @Value("${jwt.access-time}")
    private long ACCESS_EXPIRATION_TIME;
    @Value("${jwt.refresh-time}")
    private long REFRESH_EXPIRATION_TIME;

    public String accessToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
                .withClaim("id", user.getId())
                .withClaim("providerId", user.getProviderId())
                .withClaim("nickname", user.getNickname())
                .withClaim("auth", user.getRole())
                .sign(Algorithm.HMAC512(SECRET));
    }

    public String refreshToken() {
        return JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SECRET));
    }
}
