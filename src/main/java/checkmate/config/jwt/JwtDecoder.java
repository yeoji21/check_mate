package checkmate.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtDecoder {
    @Value("${jwt.secret-key}")
    private String SECRET;

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC512(SECRET))
                .build()
                .verify(token);
    }

    public String getProviderId(String accessToken) {
        return JWT.decode(accessToken).getClaim("providerId").asString();
    }
}
