package checkmate.common;

import checkmate.exception.JsonConvertingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class GoalMemberInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        if (!hasGoalMemberAnnotation((HandlerMethod) handler)) return true;

        long goalId = getGoalIdInRequest(request);
        System.out.println(goalId);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private long getGoalIdInRequest(HttpServletRequest request) throws IOException {
        long goalId = -1;
        String goalIdInParam = request.getParameter("goalId");
        if (goalIdInParam != null) {
            goalId = Long.parseLong(goalIdInParam);
        } else {
            goalId = getGoalIdInBody(request);
        }
        // TODO: 2023/02/16 EXCEPTION
        if (goalId == -1) throw new IllegalArgumentException();
        return goalId;
    }

    private long getGoalIdInBody(HttpServletRequest request) throws IOException {
        long goalId;
        try {
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            String requestBodyString = requestBody.toString();
            JsonNode jsonNode = objectMapper.readTree(requestBodyString);
            goalId = jsonNode.get("goalId").asLong();
        } catch (JsonProcessingException | NullPointerException e) {
            throw new JsonConvertingException(e, e.getMessage());
        }
        return goalId;
    }

    private boolean hasGoalMemberAnnotation(HandlerMethod handler) {
        return handler.hasMethodAnnotation(GoalMember.class);
    }
}
