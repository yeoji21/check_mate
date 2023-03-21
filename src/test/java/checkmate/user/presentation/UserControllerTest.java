package checkmate.user.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.presentation.dto.request.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends ControllerTest {
    @WithMockAuthUser
    @Test
    @DisplayName("닉네임 변경 API")
    void updateNickname() throws Exception {
        given(userDtoMapper.toCommand(any(Long.class), any(UserNicknameModifyDto.class)))
                .willReturn(new UserNicknameModifyCommand(0L, ""));

        mockMvc.perform(patch("/users/nickname")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserNicknameModifyDto("새로운 닉네임"))))
                .andExpect(status().isOk())
                .andDo(document("user-nickname-update",
                        nicknameUpdateRequestFieldsSnippet()
                ));
        verify(userCommandService).nicknameUpdate(any(UserNicknameModifyCommand.class));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("닉네임 중복 확인 API")
    void nicknameDuplicateCheck() throws Exception {
        mockMvc.perform(get("/users/exists")
                        .queryParam("nickname", "uniqueNickname")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-nickname-check",
                        nicknameCheckRequestParametersSnippet()
                ));
        verify(userQueryService).existsNicknameCheck(any(String.class));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("회원 가입 API")
    void signUp() throws Exception {
        UserSignUpDto dto = UserSignUpDto.builder()
                .userIdentifier("userIdentifier")
                .username("여지원")
                .nickname("yeoz1")
                .emailAddress("test@naverLogin.com")
                .build();

        UserSignUpCommand command = UserSignUpCommand.builder()
                .providerId(dto.getUserIdentifier())
                .nickname(dto.getNickname())
                .emailAddress(dto.getEmailAddress())
                .username(dto.getUsername())
                .build();

        given(userDtoMapper.toCommand(any(UserSignUpDto.class))).willReturn(command);

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(document("user-sign-up",
                        signUpRequestFieldsSnippet()
                ));

        verify(userCommandService).signUp(any(UserSignUpCommand.class));
    }

    @WithMockAuthUser
    @Test
    void 카카오_회원가입_테스트() throws Exception {
        KakaoSignUpDto dto = KakaoSignUpDto.builder()
                .providerId("providerId")
                .username("여지원")
                .nickname("yeoz1")
                .emailAddress("test@naverLogin.com")
                .fcmToken("fcmToken")
                .build();

        given(userDtoMapper.toCommand(any(KakaoSignUpDto.class))).willReturn(UserSignUpCommand.builder().build());

        mockMvc.perform(post("/users/kakao")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(document("user-sign-in",
                        setUserRegisterRequestFields()
                ));

        verify(userCommandService).signUp(any(UserSignUpCommand.class));
    }

    @WithMockAuthUser
    @Test
    void 구글_회원가입_테스트() throws Exception {
        GoogleSignUpDto dto = GoogleSignUpDto.builder()
                .providerId("providerId")
                .username("여지원")
                .nickname("yeoz1")
                .emailAddress("test@naverLogin.com")
                .fcmToken("fcmToken")
                .build();

        given(userDtoMapper.toCommand(any(GoogleSignUpDto.class))).willReturn(UserSignUpCommand.builder().build());

        mockMvc.perform(post("/users/google")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(document("user-sign-in",
                        setUserRegisterRequestFields()
                ));

        verify(userCommandService).signUp(any(UserSignUpCommand.class));
    }

    @WithMockAuthUser
    @Test
    void 네이버_회원가입_테스트() throws Exception {
        NaverSignUpDto dto = NaverSignUpDto.builder()
                .providerId("providerId")
                .username("여지원")
                .nickname("yeoz1")
                .emailAddress("test@naverLogin.com")
                .fcmToken("fcmToken")
                .build();

        given(userDtoMapper.toCommand(any(NaverSignUpDto.class))).willReturn(UserSignUpCommand.builder().build());

        mockMvc.perform(post("/users/naver")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(document("user-sign-in",
                        setUserRegisterRequestFields()
                ));

        verify(userCommandService).signUp(any(UserSignUpCommand.class));
    }

    private RequestFieldsSnippet signUpRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("userIdentifier").type(JsonFieldType.STRING).description("유저 식별자"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("유저의 이름"),
                fieldWithPath("emailAddress").type(JsonFieldType.STRING).description("유저의 이메일"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저의 닉네임"));
    }

    private RequestParametersSnippet nicknameCheckRequestParametersSnippet() {
        return requestParameters(parameterWithName("nickname").description("중복을 확인할 닉네임"));
    }

    private RequestFieldsSnippet nicknameUpdateRequestFieldsSnippet() {
        return requestFields(fieldWithPath("nickname").description("새로운 닉네임"));
    }

    private RequestFieldsSnippet setUserRegisterRequestFields() {
        return requestFields(
                fieldWithPath("providerId").type(JsonFieldType.STRING).description("유저 식별을 위한 id 값"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("유저의 실명"),
                fieldWithPath("emailAddress").type(JsonFieldType.STRING).description("유저의 이메일"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저가 앱에서 사용할 닉네임"),
                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("유저의 FCM 토큰"));
    }
}