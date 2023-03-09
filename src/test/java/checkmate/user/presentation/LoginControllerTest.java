package checkmate.user.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.user.presentation.dto.request.GoogleLoginDto;
import checkmate.user.presentation.dto.request.KakaoLoginDto;
import checkmate.user.presentation.dto.request.NaverLoginDto;
import checkmate.user.presentation.dto.request.TokenReissueDto;
import checkmate.user.presentation.dto.response.LoginTokenResponse;
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
    @DisplayName("로그인 토큰 재발급 API")
    void tokenReissue() throws Exception {
        TokenReissueDto dto = getTokenReissueDto();
        LoginTokenResponse response = getLoginTokenResponse();
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
    @DisplayName("로그아웃 API")
    void logout() throws Exception {
        mockMvc.perform(delete("/login/logout")
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
        LoginTokenResponse response = createLoginTokenResponse();

        given(loginService.login(any())).willReturn(response);

        mockMvc.perform(post("/login/google")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-google",
                        loginRequestFieldsSnippet(),
                        loginResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void 네이버_로그인_성공_테스트() throws Exception {
        NaverLoginDto loginDto = new NaverLoginDto("providerId", "test FcmToken");
        LoginTokenResponse response = createLoginTokenResponse();

        when(loginService.login(any())).thenReturn(response);

        mockMvc.perform(post("/login/naver")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-naver",
                        loginRequestFieldsSnippet(),
                        loginResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void 카카오_로그인_성공_테스트() throws Exception {
        KakaoLoginDto loginDto = new KakaoLoginDto("providerId", "testFcmToken");
        LoginTokenResponse response = createLoginTokenResponse();

        given(loginService.login(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/login/kakao").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("login-kakao",
                        loginRequestFieldsSnippet(),
                        loginResponseFieldsSnippet()
                ));
    }

    private ResponseFieldsSnippet loginResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("로그인 성공 후 발급하는 accessToken"),
                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("로그인 성공 후 발급하는 refreshToken")
        );
    }

    private RequestFieldsSnippet loginRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("providerId").type(JsonFieldType.STRING).description("로그인 시도하는 소셜 고유 아이디"),
                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("로그인한 기기의 fcmToken")
        );
    }

    private ResponseFieldsSnippet tokenReissueResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("새로운 refresh token"),
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("새로운 access token")
        );
    }

    private LoginTokenResponse createLoginTokenResponse() {
        return LoginTokenResponse.builder()
                .accessToken("testAccessToken")
                .refreshToken("testRefreshToken")
                .build();
    }

    private RequestFieldsSnippet tokenReissueRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("기존 refresh token"),
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("기존 access token"));
    }

    private LoginTokenResponse getLoginTokenResponse() {
        return LoginTokenResponse.builder()
                .refreshToken("after refresh token")
                .accessToken("after access token")
                .build();
    }

    private TokenReissueDto getTokenReissueDto() {
        return TokenReissueDto.builder()
                .refreshToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE2NTI2MTE2MTF9.Hj9kuCtbxEQSdXgtGhKf0PnaXBNw4vtzeZ49fm24dREbRF7mOOw634ykk6aO0VjeuinikNVMI0xP5zURZj93OA")
                .accessToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiLsl6zsp4Dsm5AiLCJhdXRoIjoiUk9MRV9VU0VSIiwicHJvdmlkZXJJZCI6IktBS0FPXzIwODcxNjM5NzUiLCJuaWNrbmFtZSI6IuynhOuLrOuemCIsImlkIjoxMiwiZXhwIjoxNjUxMjE4MjMzfQ.3_oRpdYdy4dHr9myPS8a032BNS0Acjt9SAqJ3E0yvWU19NFUN9nOQSZWge4cxX5kWucoZ-AAPKDnzcEzyfDEQA")
                .build();
    }
}