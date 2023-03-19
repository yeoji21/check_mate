package checkmate.config.jwt;

import checkmate.config.auth.AuthConstants;
import checkmate.config.auth.JwtUserDetails;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtVerifier jwtVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authorizationHeader = request.getHeader(AuthConstants.HEADER_STRING.getValue());
        log.info("Header Authorization : {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith(AuthConstants.TOKEN_PREFIX.getValue())) {
            chain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.replace(AuthConstants.TOKEN_PREFIX.getValue(), "");
        try {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            JwtUserDetails jwtUserDetails = new JwtUserDetails(decodedJWT);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (SecurityException | MalformedJwtException e) {
            request.setAttribute("exception", JwtException.WRONG_TYPE_TOKEN.name());
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", JwtException.EXPIRED_TOKEN.name());
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", JwtException.UNSUPPORTED_TOKEN.name());
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", JwtException.WRONG_TOKEN.name());
        } catch (Exception e) {
            log.error("JwtFilter - doFilterInternal() : {}", e.getMessage());
            request.setAttribute("exception", JwtException.UNKNOWN_ERROR.name());
        }
        chain.doFilter(request, response);
    }

}
