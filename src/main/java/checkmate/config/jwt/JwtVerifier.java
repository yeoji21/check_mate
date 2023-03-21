package checkmate.config.jwt;

import checkmate.config.auth.AuthConstants;
import checkmate.exception.BusinessException;
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
public class JwtVerifier {
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${jwt.secret-key}")
    private String SECRET;

    public DecodedJWT verify(String token) {
        try {
            return JWT.require(Algorithm.HMAC512(SECRET))
                    .build()
                    .verify(token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_VERIFY_FAIL);
        }
    }
    
    public void verifyRefeshToken(String providerId, String refreshToken) {
        Optional<String> findRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(providerId));
        findRefreshToken.ifPresentOrElse(findToken -> {
                    if (!findToken.equals(AuthConstants.TOKEN_PREFIX.getValue() + refreshToken))
                        throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                },
                () -> {
                    throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });
    }

    public void expireRefreshToken(String providerId) {
        redisTemplate.delete(providerId);
    }

    public String parseProviderId(String accessToken) {
        return JWT.decode(accessToken).getClaim("providerId").asString();
    }
}
