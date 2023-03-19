package checkmate.config.jwt;

import checkmate.config.auth.AuthConstants;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtDecoder {
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${jwt.secret-key}")
    private String SECRET;

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC512(SECRET))
                .build()
                .verify(token);
    }

    // TODO: 2023/03/20 TEST
    public String validateRefeshToken(String accessToken, String refreshToken) {
        String providerId = getProviderId(accessToken);
        Optional<String> findRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(providerId));
        findRefreshToken.ifPresentOrElse(
                findToken -> {
                    if (!findToken.equals(AuthConstants.TOKEN_PREFIX.getValue() + refreshToken))
                        throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                },
                () -> {
                    throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });
        return providerId;
    }

    private String getProviderId(String accessToken) {
        return JWT.decode(accessToken).getClaim("providerId").asString();
    }
}
