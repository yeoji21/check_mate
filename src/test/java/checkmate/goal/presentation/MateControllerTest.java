package checkmate.goal.presentation;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.response.GoalDetailResult;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfoResult;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.presentation.dto.MateInviteDto;
import checkmate.mate.presentation.dto.MateInviteReplyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MateControllerTest extends ControllerTest {

    @WithMockAuthUser
    @Test
    void goalDetailResultFind() throws Exception {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

        GoalDetailResult result = new GoalDetailResult(teamMate,
                List.of(LocalDate.now().minusDays(1), LocalDate.now()),
                List.of(new MateUploadInfo(1L, 1L, LocalDate.now(), "user")));

        given(mateQueryService.findGoalDetailResult(any(Long.class), any(Long.class)))
                .willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders
                        .get("/goal/detail/{goalId}", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("goal-detail-result",
                        goalDetailResultResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void 팀원_초대_테스트() throws Exception {
        MateInviteDto request = new MateInviteDto(1L, "yeoz1");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/mate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("invite-team mate", getInviteTeamMateRequest()));
        verify(mateCommandService).inviteTeamMate(any());
    }

    @WithMockAuthUser
    @Test
    void 초대_응답_수락() throws Exception {
        MateInviteReplyDto dto = new MateInviteReplyDto(1L);
        MateAcceptResult result = new MateAcceptResult(1L, 1L);

        given(mateCommandService.inviteAccept(any())).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mate/accept")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("invite-accept",
                        requestFields(
                                fieldWithPath("notificationId").type(JsonFieldType.NUMBER).description("notificationId")
                        ),
                        responseFields(
                                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("goalId"),
                                fieldWithPath("mateId").type(JsonFieldType.NUMBER).description("mateId")
                        )));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("초대 응답 거절")
    void inviteReject() throws Exception {
        MateInviteReplyDto request = new MateInviteReplyDto(1L);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mate/reject")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("invite-reject",
                        requestFields(
                                fieldWithPath("notificationId").type(JsonFieldType.NUMBER).description("notificationId")
                        )
                ));
    }

    @WithMockAuthUser
    @Test
    void 목표_진행률_조회_테스트() throws Exception {
        double result = 20.0;
        given(mateQueryService.getProgressPercent(1L)).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/mate/{mateId}/progress", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(result)))
                .andDo(document("progress-percent",
                        pathParameters(parameterWithName("mateId").description("mateId"))
                ));
    }

    @WithMockAuthUser
    @Test
    void 유저의_성공한_목표_목록_조회_테스트() throws Exception {
        List<GoalHistoryInfo> goalHistoryInfoList = getGoalHistoryInfoList();
        GoalHistoryInfoResult result = new GoalHistoryInfoResult(goalHistoryInfoList);

        given(mateQueryService.findHistoryGoalInfo(any(Long.class))).willReturn(result);

        mockMvc.perform(get("/goal/history")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("goal-history",
                        historyResponseFieldsSnippet())
                );
    }

    private List<GoalHistoryInfo> getGoalHistoryInfoList() {
        TeamMate teamMate1 = TestEntityFactory.goal(1L, "goal1")
                .join(TestEntityFactory.user(1L, "user1"));
        TeamMate teamMate2 = TestEntityFactory.goal(2L, "goal2")
                .join(TestEntityFactory.user(2L, "user2"));

        return List.of(new GoalHistoryInfo(teamMate1, List.of("nickname")),
                new GoalHistoryInfo(teamMate2, List.of("nickname")));
    }

    private ResponseFieldsSnippet historyResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("info[].goalId").description("목표 id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("info[].startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("info[].endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("info[].checkDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("info[].appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("info[].achievementRate").type(JsonFieldType.NUMBER).description("유저의 최종 성취율"),
                fieldWithPath("info[].teamMateNicknames").type(JsonFieldType.ARRAY).description("팀원들의 닉네임")
        );
    }

    @WithMockAuthUser
    @Test
    void 팀원의_목표_수행_캘린더_조회() throws Exception {
        MateScheduleInfo calendarInfo = MateScheduleInfo.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .weekDays(1111111)
                .uploadedDates(List.of(LocalDate.now()))
                .build();

        given(mateQueryService.getCalenderInfo(1L)).willReturn(calendarInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/mate/{mateId}/calendar", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(calendarInfo)))
                .andDo(document("mate-calendar",
                        pathParameters(parameterWithName("mateId").description("mateId")),
                        teamMateCalendarResponseFieldsSnippet()
                ));
    }

    private ResponseFieldsSnippet goalDetailResultResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("ID 값"),
                fieldWithPath("teamMates").description("목표에 속한 팀원들"),
                fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("inviteable").type(JsonFieldType.BOOLEAN).description("초대할 수 있는 목표인지"),
                fieldWithPath("goalStatus").type(JsonFieldType.STRING).description("목표 상태"),
                fieldWithPath("teamMates[].mateId").description("팀메이트 id"),
                fieldWithPath("teamMates[].userId").description("유저 id"),
                fieldWithPath("teamMates[].nickname").description("유저의 닉네임"),
                fieldWithPath("teamMates[].uploaded").description("이미 업로드했는지"),
                fieldWithPath("uploadable.uploaded").description("목표를 조회한 유저가 이미 업로드했는지"),
                fieldWithPath("uploadable.uploadable").description("목표를 조회한 유저가 목표를 업로드할 수 있는지"),
                fieldWithPath("uploadable.workingDay").description("업로드하는 날이 맞는지"),
                fieldWithPath("uploadable.timeOver").description("인증 시간이 초과되었는지"),
                fieldWithPath("goalSchedule").type(JsonFieldType.STRING).description("목표 수행 일정"),
                fieldWithPath("teamMateSchedule").type(JsonFieldType.STRING).description("팀원의 목표 수행 일정"),
                fieldWithPath("progress").type(JsonFieldType.NUMBER).description("팀원의 목표 수행 진행률")
        );
    }

    private ResponseFieldsSnippet teamMateCalendarResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("목표 수행 시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("목표 수행 종료일"),
                fieldWithPath("goalSchedule").type(JsonFieldType.STRING).description("목표 수행 일정"),
                fieldWithPath("mateSchedule").type(JsonFieldType.STRING).description("팀원의 목표 수행 일정")
        );
    }

    private RequestFieldsSnippet getInviteTeamMateRequest() {
        return requestFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("goalId"),
                fieldWithPath("inviteeNickname").type(JsonFieldType.STRING).description("초대할 사람의 닉네임")
        );
    }
}