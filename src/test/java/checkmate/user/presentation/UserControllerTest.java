package checkmate.user.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.presentation.dto.request.GoogleSignUpDto;
import checkmate.user.presentation.dto.request.KakaoSignUpDto;
import checkmate.user.presentation.dto.request.NaverSignUpDto;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

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
    void 카카오_회원가입_테스트() throws Exception {
        KakaoSignUpDto dto = KakaoSignUpDto.builder()
                .providerId("providerId")
                .username("여지원")
                .nickname("yeoz1")
                .emailAddress("test@naverLogin.com")
                .fcmToken("fcmToken")
                .build();

        given(userDtoMapper.toCommand(any(KakaoSignUpDto.class))).willReturn(UserSignUpCommand.builder().build());

        mockMvc.perform(post("/user/kakao")
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

        mockMvc.perform(post("/user/google")
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

        mockMvc.perform(post("/user/naver")
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
    void 닉네임_변경_테스트() throws Exception {
        UserNicknameModifyDto request = new UserNicknameModifyDto("새로운 닉네임");

        given(userDtoMapper.toCommand(any(Long.class), any(UserNicknameModifyDto.class)))
                .willReturn(new UserNicknameModifyCommand(0L, ""));

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

    @WithMockAuthUser
    @Test
    void 닉네임_중복_통과_테스트() throws Exception {
        mockMvc.perform(get("/user/exists")
                        .queryParam("nickname", "uniqueNickname")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("nickname-dup",
                        requestParameters(parameterWithName("nickname").description("중복 확인을 하고자하는 닉네임"))
                ));
        verify(userQueryService).existsNicknameCheck(any(String.class));
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