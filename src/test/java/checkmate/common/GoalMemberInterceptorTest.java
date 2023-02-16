package checkmate.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GoalMemberInterceptorTest {
    @Mock
    private HandlerMethod handlerMethod;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GoalMemberInterceptor interceptor;

    @Test
    @DisplayName("request param에 goalId가 존재하는 경우")
    void goalId_in_param() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("goalId", "123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(handlerMethod.hasMethodAnnotation(GoalMember.class)).willReturn(true);

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

        //when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        //then
        assertThat(result).isTrue();
    }
}