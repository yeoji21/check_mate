package checkmate.user.presentation;

import checkmate.ExampleSimpleRestDocsTest;
import checkmate.config.WithMockAuthUser;
import checkmate.user.application.LoginService;
import checkmate.user.application.UserCommandService;
import checkmate.user.application.UserFindService;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.presentation.dto.UserDtoMapper;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@ExtendWith(MockitoExtension.class)
public class ExampleControllerTest extends ExampleSimpleRestDocsTest {
    private MockMvc mockMvc;

    @Mock private UserFindService userFindService;
    @Mock private UserCommandService userCommandService;
    @Mock private LoginService loginService;
    @Mock private UserDtoMapper userDtoMapper;
    @InjectMocks private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = mockMvc(userController);
    }

    @WithMockAuthUser
    @Test
    void 닉네임_변경_테스트() throws Exception{
        UserNicknameModifyDto request = new UserNicknameModifyDto("새로운 닉네임");

        mockMvc.perform(patch("/user/nickname")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("nickname-modify",
                        requestFields(fieldWithPath("nickname").description("새로운 닉네임"))
                ));
        verify(userCommandService).nicknameUpdate(any(UserNicknameModifyCommand.class));
    }
}
