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
    EXPIRED_TOKEN(UNAUTHORIZED, "만료됨"),
    WRONG_TOKEN(UNAUTHORIZED, "잘못된 형식"),
    EMPTY_TOKEN(UNAUTHORIZED, "토큰이 없음"),
    UNKNOWN_ERROR(UNAUTHORIZED, "알 수 없는 문제");

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
