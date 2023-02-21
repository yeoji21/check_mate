package checkmate.common;

import checkmate.common.interceptor.GoalIdRoute;
import checkmate.common.interceptor.GoalMember;
import checkmate.common.interceptor.GoalMemberInterceptor;
import checkmate.config.auth.JwtUserDetails;
import checkmate.goal.domain.TeamMateRepository;
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

import java.nio.charset.StandardCharsets;
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
    private TeamMateRepository teamMateRepository;
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
    }

    @Test
    @DisplayName("request param에 goalId가 존재하는 경우")
    void goalId_in_param() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("goalId", "123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(handlerMethod.hasMethodAnnotation(GoalMember.class)).willReturn(true);
        given(handlerMethod.getMethodAnnotation(GoalMember.class)).willReturn(mockAnnotation);
        given(mockAnnotation.value()).willReturn(GoalIdRoute.REQUEST_PARAM);
        given(teamMateRepository.isExistTeamMate(anyLong(), anyLong())).willReturn(true);

        //when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("request body에 goalId가 존재하는 경우")
    void goalId_in_body() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("{\"goalId\": 123}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(handlerMethod.hasMethodAnnotation(GoalMember.class)).willReturn(true);
        given(handlerMethod.getMethodAnnotation(GoalMember.class)).willReturn(mockAnnotation);
        given(mockAnnotation.value()).willReturn(GoalIdRoute.REQUEST_BODY);
        given(teamMateRepository.isExistTeamMate(anyLong(), anyLong())).willReturn(true);

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
        given(handlerMethod.hasMethodAnnotation(GoalMember.class)).willReturn(true);
        given(handlerMethod.getMethodAnnotation(GoalMember.class)).willReturn(mockAnnotation);
        given(mockAnnotation.value()).willReturn(GoalIdRoute.PATH_VARIABLE);
        given(teamMateRepository.isExistTeamMate(anyLong(), anyLong())).willReturn(true);

        //when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        //then
        assertThat(result).isTrue();
    }
}