package checkmate.config.jwt;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException){
        JwtException exception = getJwtException(request);
        if (exception != JwtException.EMPTY_TOKEN) exception.setResponse(response);
    }

    private JwtException getJwtException(HttpServletRequest request) {
        String exception = (String) request.getAttribute("exception");
        return exception == null ? JwtException.EMPTY_TOKEN : JwtException.valueOf(exception);
    }
}
