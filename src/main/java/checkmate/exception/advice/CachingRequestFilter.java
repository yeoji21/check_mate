package checkmate.exception.advice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
//@Component
public class CachingRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletRequest requestToCache = new ContentCachingRequestWrapper(request);
        chain.doFilter(requestToCache, response);
    }
}
