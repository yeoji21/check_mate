package checkmate.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.user.application.dto.CheckedGoalInfo;
import checkmate.user.application.dto.DailySchedule;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.presentation.dto.UserScheduleResponse;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import checkmate.user.presentation.dto.request.UserSignUpDto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;

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
            .andDo(document("user-nickname-modify",
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
        UserSignUpDto dto = createUserSignUpDto();
        UserSignUpCommand command = UserSignUpCommand.builder()
            .identifier("identifier")
            .username("username")
            .emailAddress("email@test.com")
            .nickname("nickname")
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
    @DisplayName("유저의 주차별 목표 개수 조회 API")
    void findUserWeeklyGoalSchedule() throws Exception {
        LocalDate requestDate = LocalDate.now();
        when(userQueryService.getWeeklySchdule(1L, requestDate)).thenReturn(
            userScheduleResponse(requestDate));

        mockMvc.perform(get("/users/weekly-schedule")
                .param("date", requestDate.toString())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(
                objectMapper.writeValueAsString(userScheduleResponse(requestDate))))
            .andDo(document("user-weekly-schedule",
                requestParameters(parameterWithName("date").description("요청일")),
                userScheduleResponseFieldsSnippet()));
    }

    private UserScheduleResponse userScheduleResponse(LocalDate requestDate) {
        DailySchedule today = DailySchedule.builder()
            .date(requestDate)
            .goals(List.of(
                CheckedGoalInfo.builder()
                    .goalId(1L)
                    .checked(true)
                    .build(),
                CheckedGoalInfo.builder()
                    .goalId(2L)
                    .checked(true)
                    .build(),
                CheckedGoalInfo.builder()
                    .goalId(3L)
                    .checked(true)
                    .build()))
            .build();
        DailySchedule yesterday = DailySchedule.builder()
            .date(requestDate.minusDays(1))
            .goals(List.of(
                CheckedGoalInfo.builder()
                    .goalId(1L)
                    .checked(false)
                    .build(),
                CheckedGoalInfo.builder()
                    .goalId(2L)
                    .checked(true)
                    .build(),
                CheckedGoalInfo.builder()
                    .goalId(3L)
                    .checked(true)
                    .build()))
            .build();
        DailySchedule tomorrow = DailySchedule.builder()
            .date(requestDate.plusDays(1))
            .goals(List.of(
                CheckedGoalInfo.builder()
                    .goalId(3L)
                    .checked(false)
                    .build()))
            .build();

        return UserScheduleResponse.builder()
            .requestDate(requestDate)
            .schedule(List.of(yesterday, today, tomorrow))
            .build();
    }

    private UserSignUpDto createUserSignUpDto() {
        return UserSignUpDto.builder()
            .identifier("identifier")
            .username("username")
            .nickname("yeoz1")
            .emailAddress("test@naverLogin.com")
            .build();
    }

    private ResponseFieldsSnippet userScheduleResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("requestDate").type(JsonFieldType.STRING).description("요청일"),
            fieldWithPath("schedule").type(JsonFieldType.ARRAY).description("목표 스케줄 목록"),
            fieldWithPath("schedule[].date").type(JsonFieldType.STRING).description("날짜"),
            fieldWithPath("schedule[].goals").type(JsonFieldType.ARRAY).description("목표 목록"),
            fieldWithPath("schedule[].goals[].goalId").type(JsonFieldType.NUMBER)
                .description("goalId"),
            fieldWithPath("schedule[].goals[].checked").type(JsonFieldType.BOOLEAN)
                .description("인증 여부")
        );
    }

    private RequestFieldsSnippet signUpRequestFieldsSnippet() {
        return requestFields(
            fieldWithPath("identifier").type(JsonFieldType.STRING).description("유저 식별자"),
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

}