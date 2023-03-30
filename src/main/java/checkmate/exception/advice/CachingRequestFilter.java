package checkmate.exception.advice;


import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class CachingRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (isMultipartRequest(request)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest requestToCache = new ContentCachingRequestWrapper(request);
        chain.doFilter(requestToCache, response);
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart");
    }
}
