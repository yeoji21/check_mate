package checkmate.common.interceptor;

import checkmate.config.auth.JwtUserDetails;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.infra.MateQueryDao;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Profile("!test")
@RequiredArgsConstructor
@Component
public class GoalMemberInterceptor implements HandlerInterceptor {

    private final MateQueryDao mateQueryDao;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler) {
        if (handler instanceof HandlerMethod handlerMethod &&
            hasGoalMemberAnnotation(handlerMethod) &&
            !isGoalMember(handlerMethod, request)) {
            throw new IllegalArgumentException("존재하지 않는 팀원");
        }
        return true;
    }

    private boolean hasGoalMemberAnnotation(HandlerMethod handler) {
        return handler.hasMethodAnnotation(GoalMember.class);
    }

    private boolean isGoalMember(HandlerMethod handlerMethod, HttpServletRequest request) {
        long goalId = getRequestGoalId(handlerMethod, request);
        long userId = getRequestUserId();
        return mateQueryDao.existOngoingMate(goalId, userId);
    }

    private long getRequestGoalId(HandlerMethod handlerMethod, HttpServletRequest request) {
        String goalId = switch (getGoalIdType(handlerMethod)) {
            case REQUEST_PARAM -> getFromRequestParam(request);
            case PATH_VARIABLE -> getFromPathVariable(request);
        };
        if (goalId == null) {
            throw new BusinessException(ErrorCode.MATE_NOT_FOUND);
        }
        return Long.parseLong(goalId);
    }

    private GoalId getGoalIdType(HandlerMethod handlerMethod) {
        return handlerMethod.getMethodAnnotation(GoalMember.class).value();
    }

    private String getFromRequestParam(HttpServletRequest request) {
        return request.getParameter("goalId");
    }

    private String getFromPathVariable(HttpServletRequest request) {
        Map<String, String> attribute = (Map<String, String>) request.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return attribute == null ? null : attribute.get("goalId");
    }

    private long getRequestUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}
