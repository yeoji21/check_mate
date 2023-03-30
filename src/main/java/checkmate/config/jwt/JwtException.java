package checkmate.config.jwt;

import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Getter
@AllArgsConstructor
public enum JwtException {
    WRONG_TYPE_TOKEN(UNAUTHORIZED, "잘못된 형식"),
    EXPIRED_TOKEN(UNAUTHORIZED, "기간 만료"),
    UNSUPPORTED_TOKEN(UNAUTHORIZED, "지원되지 않는 토큰"),
    WRONG_TOKEN(UNAUTHORIZED, "잘못된 토큰"),
    EMPTY_TOKEN(UNAUTHORIZED, "빈 토큰"),
    UNKNOWN_ERROR(UNAUTHORIZED, "토큰 에러");

    private final HttpStatus httpStatus;
    private final String detail;

    public void setResponse(HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject responseJson = createResponseBody();
        try {
            response.setStatus(401);
            response.getWriter().print(responseJson);
        } catch (IOException e) {
            log.error("JWT IOException on {}", e.getMessage());
        }
    }

    public JSONObject createResponseBody() {
        JSONObject responseJson = new JSONObject();
        responseJson.put("status", this.getHttpStatus().value());
        responseJson.put("error", this.getHttpStatus().name());
        responseJson.put("code", this.name());
        responseJson.put("message", this.getDetail());
        return responseJson;
    }
}
