package checkmate.common.interceptor;

import checkmate.config.auth.JwtUserDetails;
import checkmate.exception.BusinessException;
import checkmate.exception.JsonConvertingException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.MateRepository;
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
    private final MateRepository mateRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;
        if (!hasGoalMemberAnnotation(handlerMethod)) return true;

        long goalId = getGoalIdInRequest(handlerMethod, request);
        long userId = getRequestUserId();
        boolean exist = mateRepository.existOngoingMate(goalId, userId);
        if (!exist) throw new IllegalArgumentException("존재하지 않는 팀원");
        return true;
    }

    private long getRequestUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }

    private long getGoalIdInRequest(HandlerMethod handlerMethod,
                                    HttpServletRequest request) throws IOException {
        GoalIdRoute route = handlerMethod.getMethodAnnotation(GoalMember.class).value();
        Long goalId = switch (route) {
            case REQUEST_PARAM -> Long.parseLong(request.getParameter("goalId"));
            case REQUEST_BODY -> getFromMessageBody(request);
            case PATH_VARIABLE -> getFromPathVariable(request);
        };
        if (goalId == null) throw new BusinessException(ErrorCode.MATE_NOT_FOUND);
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
