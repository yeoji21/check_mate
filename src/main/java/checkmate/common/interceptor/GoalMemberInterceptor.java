package checkmate.common.interceptor;

import checkmate.config.auth.JwtUserDetails;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.infra.MateQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Profile("!test")
@RequiredArgsConstructor
@Component
public class GoalMemberInterceptor implements HandlerInterceptor {
    private final MateQueryDao mateQueryDao;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;
        if (!hasGoalMemberAnnotation(handlerMethod)) return true;

        long goalId = getGoalIdInRequest(handlerMethod, request);
        long userId = getRequestUserId();
        boolean exist = mateQueryDao.existOngoingMate(goalId, userId);
        if (!exist) throw new IllegalArgumentException("존재하지 않는 팀원");
        return true;
    }

    private long getRequestUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }

    private long getGoalIdInRequest(HandlerMethod handlerMethod, HttpServletRequest request) {
        GoalId route = handlerMethod.getMethodAnnotation(GoalMember.class).value();
        Long goalId = switch (route) {
            case REQUEST_PARAM -> Long.parseLong(request.getParameter("goalId"));
            case PATH_VARIABLE -> getFromPathVariable(request);
        };
        if (goalId == null) throw new BusinessException(ErrorCode.MATE_NOT_FOUND);
        return goalId;
    }

    private Long getFromPathVariable(HttpServletRequest request) {
        Map<String, String> attribute = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return attribute == null ? null : Long.parseLong(attribute.get("goalId"));
    }

    private boolean hasGoalMemberAnnotation(HandlerMethod handler) {
        return handler.hasMethodAnnotation(GoalMember.class);
    }
}
