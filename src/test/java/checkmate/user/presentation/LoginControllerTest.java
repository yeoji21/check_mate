package checkmate.user.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.user.presentation.dto.request.*;
import checkmate.user.presentation.dto.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTest extends ControllerTest {
    @WithMockAuthUser
    @Test
    @DisplayName("로그인 API")
    void login() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto("identifier", "test fcmToken");
        LoginResponse response = createLoginResponse();

        given(loginService.login(any())).willReturn(response);

        mockMvc.perform(post("/users/login")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login",
                        loginRequestFieldsSnippet(),
                        loginResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("로그인 토큰 재발급 API")
    void tokenReissue() throws Exception {
        TokenReissueDto dto = createTokenReissueDto();
        LoginResponse response = createLoginResponse();
        given(loginService.reissueToken(any())).willReturn(response);

        mockMvc.perform(post("/login/reissue")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-token-reissue",
                        tokenReissueRequestFieldsSnippet(),
                        tokenReissueResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        mockMvc.perform(delete("/user/logout")
                        .contentType(APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(document("logout"));

        verify(loginService).logout(any(Long.class));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("구글 로그인 API")
    void loginGoogle() throws Exception {
        GoogleLoginDto loginDto = new GoogleLoginDto("providerId", "test FcmToken");
        LoginResponse response = createLoginResponse();

        given(loginService.login_v1(any())).willReturn(response);

        mockMvc.perform(post("/login/google")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-google",
                        requestFields(
                                fieldWithPath("providerId").type(JsonFieldType.STRING).description("로그인 시도하는 소셜 고유 아이디"),
                                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("로그인한 기기의 fcmToken")
                        ),
                        loginResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void 네이버_로그인_성공_테스트() throws Exception {
        NaverLoginDto loginDto = new NaverLoginDto("providerId", "test FcmToken");
        LoginResponse response = createLoginResponse();

        when(loginService.login_v1(any())).thenReturn(response);

        mockMvc.perform(post("/login/naver")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-naver",
                        requestFields(
                                fieldWithPath("providerId").type(JsonFieldType.STRING).description("로그인 시도하는 소셜 고유 아이디"),
                                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("로그인한 기기의 fcmToken")
                        ),
                        loginResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void 카카오_로그인_성공_테스트() throws Exception {
        KakaoLoginDto loginDto = new KakaoLoginDto("providerId", "testFcmToken");
        LoginResponse response = createLoginResponse();

        given(loginService.login_v1(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/login/kakao").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-kakao",
                        requestFields(
                                fieldWithPath("providerId").type(JsonFieldType.STRING).description("로그인 시도하는 소셜 고유 아이디"),
                                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("로그인한 기기의 fcmToken")
                        ),
                        loginResponseFieldsSnippet()
                ));
    }

    private ResponseFieldsSnippet loginResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("로그인 성공 후 발급하는 accessToken"),
                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("로그인 성공 후 발급하는 refreshToken")
        );
    }

    private ResponseFieldsSnippet tokenReissueResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("새로운 refresh token"),
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("새로운 access token")
        );
    }

    private LoginResponse createLoginResponse() {
        return LoginResponse.builder()
                .accessToken("Bearer accessToken")
                .refreshToken("Bearer refreshToken")
                .build();
    }

    private RequestFieldsSnippet tokenReissueRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("기존 refresh token"),
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("기존 access token"));
    }

    private TokenReissueDto createTokenReissueDto() {
        return TokenReissueDto.builder()
                .refreshToken("Bearer accessToken")
                .accessToken("Bearer refreshToken")
                .build();
    }

    private RequestFieldsSnippet loginRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("identifier").type(JsonFieldType.STRING).description("로그인 시도하는 유저 식별자"),
                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("로그인한 기기의 fcmToken")
        );
    }
}