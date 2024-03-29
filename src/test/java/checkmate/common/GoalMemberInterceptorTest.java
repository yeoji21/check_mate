package checkmate.common;

import checkmate.common.interceptor.GoalId;
import checkmate.common.interceptor.GoalMember;
import checkmate.common.interceptor.GoalMemberInterceptor;
import checkmate.config.auth.JwtUserDetails;
import checkmate.mate.infra.MateQueryDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GoalMemberInterceptorTest {
    @Mock
    private HandlerMethod handlerMethod;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private MateQueryDao mateQueryDao;
    private GoalMember mockAnnotation;

    @InjectMocks
    private GoalMemberInterceptor interceptor;

    @BeforeEach
    void beforeEach() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        JwtUserDetails principal = new JwtUserDetails();
        ReflectionTestUtils.setField(principal, "id", 1L);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, "password");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        mockAnnotation = mock(GoalMember.class);

        given(handlerMethod.hasMethodAnnotation(GoalMember.class)).willReturn(true);
        given(handlerMethod.getMethodAnnotation(GoalMember.class)).willReturn(mockAnnotation);
        given(mateQueryDao.existOngoingMate(anyLong(), anyLong())).willReturn(true);
    }


    @Test
    @DisplayName("request param에 goalId가 존재하는 경우")
    void goalId_in_param() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setParameter("goalId", "123");
        given(mockAnnotation.value()).willReturn(GoalId.REQUEST_PARAM);

        //when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("path variable에 goalId가 존재하는 경우")
    void goalId_in_path_variable() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                Collections.singletonMap("goalId", "1"));
        given(mockAnnotation.value()).willReturn(GoalId.PATH_VARIABLE);

        //when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        //then
        assertThat(result).isTrue();
    }
}