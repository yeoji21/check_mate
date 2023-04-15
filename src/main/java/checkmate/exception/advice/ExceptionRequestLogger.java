package checkmate.exception.advice;

import checkmate.config.jwt.JwtException;
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
        logBuilder.append(getRequestURI(request)).append("\n");
        logBuilder.append(getExceptionName(e)).append(" -> ").append(parsingMessage(e)).append("\n");
        logBuilder.append(response.getBody()).append("\n");
        logBuilder.append(parsingHeaders(request)).append("\n");
        logBuilder.append(parsingBody(request));
        log.warn(logBuilder.toString());
    }

    public static void logging(HttpServletRequest request, JwtException exception) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(getRequestURI(request)).append("\n");
        logBuilder.append(parsingHeaders(request)).append("\n");
        logBuilder.append(exception.createResponseBody()).append("\n");
        log.warn(logBuilder.toString());
    }

    private static String parsingMessage(Exception e) {
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

    private static Map<String, Object> parsingHeaders(HttpServletRequest request) {
        Map<String, Object> headerMap = new HashMap<>();

        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private static String parsingBody(HttpServletRequest request) {
        ContentCachingRequestWrapper cachedRequest = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (cachedRequest != null) {
            byte[] buf = cachedRequest.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    return new String(buf, 0, buf.length, cachedRequest.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    return " Unsupported Encoding ";
                }
            }
        }
        return " EMPTY BODY ";
    }

    private static String getExceptionName(Exception e) {
        return "[" + e.getClass().getSimpleName() + "]";
    }

    private static String getRequestURI(HttpServletRequest request) {
        return request.getMethod() + " - " + request.getRequestURI();
    }
}
