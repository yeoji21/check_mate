package checkmate.exception.advice;

import checkmate.exception.ErrorResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ExceptionRequestLogger {
    public static void logging(ResponseEntity<ErrorResponse> response, Exception e, HttpServletRequest request) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(request.getMethod()).append("-").append(request.getRequestURI()).append("\n");
        logBuilder.append("[" + e.getClass().getSimpleName() + "] -> ").append(getExceptionMessage(e)).append("\n");
        logBuilder.append(response.getBody()).append("\n");
        logBuilder.append(getHeaders(request)).append("\n");
        logBuilder.append(getRequestBody((ContentCachingRequestWrapper) request));
        log.warn(logBuilder.toString());
    }

    private static String getExceptionMessage(Exception e) {
        String message = e.getMessage();
        if (e.getClass().equals(MethodArgumentNotValidException.class)) {
            message = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage()).collect(Collectors.joining(" and "));
        }
        if (message == null) {
            message = "EMPTY MESSAGE";
        }
        return message;
    }

    private static Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> headerMap = new HashMap<>();

        Enumeration<String> headerArray = request.getHeaderNames();
        while (headerArray.hasMoreElements()) {
            String headerName = headerArray.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    return " - ";
                }
            }
        }
        return " - ";
    }
}
