package checkmate.common;

import checkmate.config.auth.JwtUserDetails;
import checkmate.exception.JsonConvertingException;
import checkmate.goal.domain.TeamMateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@Profile("!test")
@RequiredArgsConstructor
@Component
public class GoalMemberInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;
    private final TeamMateRepository teamMateRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        if (!hasGoalMemberAnnotation((HandlerMethod) handler)) return true;

        long goalId = getGoalIdInRequest(request);
        long userId = getRequestUserId();
        boolean existTeamMate = teamMateRepository.isExistTeamMate(goalId, userId);
        if (!existTeamMate) throw new IllegalArgumentException("존재하지 않는 팀원");
        return true;
    }

    private long getRequestUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }

    // TODO: 2023/02/18 어노테이션 내 필드로 분기 처리?
    private long getGoalIdInRequest(HttpServletRequest request) throws IOException {
        Long goalId = null;
        String goalIdInParam = request.getParameter("goalId");
        if (goalIdInParam != null) {
            goalId = Long.parseLong(goalIdInParam);
        } else {
            goalId = getFromPathVariable(request);
        }
        if (goalId == null) {
            goalId = getFromMessageBody(request);
        }

        // TODO: 2023/02/16 EXCEPTION
        if (goalId == -1) throw new IllegalArgumentException();
        return goalId;
    }

    private Long getFromPathVariable(HttpServletRequest request) {
        Map<String, String> attribute = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return attribute == null ? null : Long.parseLong(attribute.get("goalId"));
    }

    private long getFromMessageBody(HttpServletRequest request) throws IOException {
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
