package checkmate;

import checkmate.exception.ErrorCodeController;
import checkmate.goal.application.GoalCommandService;
import checkmate.goal.application.GoalQueryService;
import checkmate.goal.application.TeamMateCommandService;
import checkmate.goal.application.TeamMateQueryService;
import checkmate.goal.presentation.GoalController;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.mate.presentation.MateController;
import checkmate.mate.presentation.dto.MateDtoMapper;
import checkmate.notification.application.NotificationQueryService;
import checkmate.notification.presentation.NotificationController;
import checkmate.notification.presentation.dto.NotificationDtoMapper;
import checkmate.post.application.PostCommandService;
import checkmate.post.application.PostQueryService;
import checkmate.post.presentation.PostController;
import checkmate.post.presentation.dto.PostDtoMapper;
import checkmate.user.application.LoginService;
import checkmate.user.application.UserCommandService;
import checkmate.user.application.UserFindService;
import checkmate.user.presentation.LoginController;
import checkmate.user.presentation.UserController;
import checkmate.user.presentation.dto.LoginDtoMapper;
import checkmate.user.presentation.dto.UserDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest({
        ErrorCodeController.class,
        GoalController.class,
        MateController.class,
        NotificationController.class,
        PostController.class,
        LoginController.class,
        UserController.class
})
public abstract class ControllerTest {
    @MockBean
    protected GoalCommandService goalCommandService;
    @MockBean
    protected GoalQueryService goalQueryService;

    @MockBean
    protected TeamMateCommandService teamMateCommandService;
    @MockBean
    protected TeamMateQueryService teamMateQueryService;
    @MockBean
    protected NotificationQueryService notificationQueryService;
    @MockBean
    protected NotificationDtoMapper notificationDtoMapper;

    @MockBean
    protected PostCommandService postCommandService;
    @MockBean
    protected PostQueryService postQueryService;

    @MockBean
    protected LoginService loginService;

    @MockBean
    protected UserFindService userFindService;
    @MockBean
    protected UserCommandService userCommandService;

    @MockBean
    protected GoalDtoMapper goalDtoMapper;
    @MockBean
    protected MateDtoMapper mateDtoMapper;
    @MockBean
    protected PostDtoMapper postDtoMapper;
    @MockBean
    protected LoginDtoMapper loginDtoMapper;

    @MockBean
    protected UserDtoMapper userDtoMapper;

    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider contextProvider) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(documentationConfiguration(contextProvider))
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print()).build();
    }
}
