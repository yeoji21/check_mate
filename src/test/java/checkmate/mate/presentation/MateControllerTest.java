package checkmate.mate.presentation;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.domain.Goal;
import checkmate.mate.application.dto.response.*;
import checkmate.mate.domain.Mate;
import checkmate.mate.presentation.dto.MateInviteDto;
import checkmate.mate.presentation.dto.MateInviteReplyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;

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
    @DisplayName("유저 특화 목표 상세 정보 조회 API")
    void findSpecifiedGoalDetailInfo() throws Exception {
        SpecifiedGoalDetailInfo result = getSpecifiedGoalDetailInfo();
        given(mateQueryService.findSpecifiedGoalDetailInfo(any(Long.class), any(Long.class)))
                .willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders
                        .get("/goals/{goalId}/detail", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("goal-specified-goals",
                        goalIdPathParametersSnippet(),
                        specifiedGoalDetailResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("성공한 목표 목록 조회 API")
    void findGoalHistoryResult() throws Exception {
        GoalHistoryInfoResult result = new GoalHistoryInfoResult(getGoalHistoryInfoList());
        given(mateQueryService.findGoalHistoryResult(any(Long.class))).willReturn(result);

        mockMvc.perform(get("/goals/history")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("goal-history",
                        historyResultResponseFieldsSnippet())
                );
    }

    @WithMockAuthUser
    @Test
    @DisplayName("팀원 초대 API")
    void inviteToGoal() throws Exception {
        MateInviteDto request = new MateInviteDto(1L, "yeoz1");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/mates")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("mate-invite",
                        inviteRequestFieldsSnippet()
                ));
        verify(mateCommandService).inviteMate(any());
    }

    @WithMockAuthUser
    @Test
    @DisplayName("초대 수락 API")
    void inviteAccept() throws Exception {
        MateInviteReplyDto dto = new MateInviteReplyDto(1L);
        MateAcceptResult result = new MateAcceptResult(1L, 1L);
        given(mateCommandService.inviteAccept(any())).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mates/accept")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("mate-invite-accept",
                        inviteReplyRequestFieldsSnippet(),
                        inviteAcceptResponseFieldsSnippet()));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("초대 응답 거절")
    void inviteReject() throws Exception {
        MateInviteReplyDto request = new MateInviteReplyDto(1L);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mates/reject")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("mate-invite-reject",
                        inviteReplyRequestFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("목표 수행 캘린더 조회 API")
    void findMateCalender() throws Exception {
        MateScheduleInfo calendarInfo = getMateScheduleInfo();
        given(mateQueryService.findCalenderInfo(1L)).willReturn(calendarInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/mates/{mateId}/calendar", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(calendarInfo)))
                .andDo(document("mate-calender",
                        mateCalenderPathParametersSnippet(),
                        nateCalenderResponseFieldsSnippet()
                ));
    }

    private ResponseFieldsSnippet nateCalenderResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("목표 수행 시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("목표 수행 종료일"),
                fieldWithPath("goalSchedule").type(JsonFieldType.STRING).description("목표 수행 일정"),
                fieldWithPath("mateSchedule").type(JsonFieldType.STRING).description("팀원의 목표 수행 일정")
        );
    }

    private PathParametersSnippet mateCalenderPathParametersSnippet() {
        return pathParameters(parameterWithName("mateId").description("팀원 ID"));
    }

    private MateScheduleInfo getMateScheduleInfo() {
        return MateScheduleInfo.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .weekDays(1111010101)
                .uploadedDates(List.of(LocalDate.now()))
                .build();
    }

    private ResponseFieldsSnippet inviteAcceptResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
                fieldWithPath("mateId").type(JsonFieldType.NUMBER).description("팀원 ID")
        );
    }

    private RequestFieldsSnippet inviteReplyRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("notificationId").type(JsonFieldType.NUMBER).description("알림 ID")
        );
    }

    private RequestFieldsSnippet inviteRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
                fieldWithPath("inviteeNickname").type(JsonFieldType.STRING).description("초대할 유저의 닉네임")
        );
    }

    private ResponseFieldsSnippet historyResultResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("goals[].goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
                fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("목표 카테고리"),
                fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("goals[].startDate").type(JsonFieldType.STRING).description("목표 시작일"),
                fieldWithPath("goals[].endDate").type(JsonFieldType.STRING).description("목표 종료일"),
                fieldWithPath("goals[].checkDays").type(JsonFieldType.STRING).description("목표 인증 요일"),
                fieldWithPath("goals[].appointmentTime").type(JsonFieldType.STRING).description("목표 인증 시간").optional(),
                fieldWithPath("goals[].achievementRate").type(JsonFieldType.NUMBER).description("유저의 최종 성취율"),
                fieldWithPath("goals[].mateNicknames").type(JsonFieldType.ARRAY).description("팀원들의 닉네임")
        );
    }

    private ResponseFieldsSnippet specifiedGoalDetailResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
                fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("status").type(JsonFieldType.STRING).description("목표 상태"),
                fieldWithPath("mates").description("목표에 속한 팀원들"),
                fieldWithPath("mates[].mateId").description("팀원 ID"),
                fieldWithPath("mates[].userId").description("유저 ID"),
                fieldWithPath("mates[].nickname").description("유저의 닉네임"),
                fieldWithPath("mates[].uploaded").description("이미 업로드했는지"),
                fieldWithPath("inviteable").type(JsonFieldType.BOOLEAN).description("초대할 수 있는 목표인지"),
                fieldWithPath("uploadable.uploaded").description("목표를 조회한 유저가 이미 인증했는지"),
                fieldWithPath("uploadable.uploadable").description("목표를 조회한 유저가 목표를 인증할 수 있는지"),
                fieldWithPath("uploadable.workingDay").description("인증하는 날이 맞는지"),
                fieldWithPath("uploadable.timeOver").description("인증 시간이 초과되었는지"),
                fieldWithPath("goalSchedule").type(JsonFieldType.STRING).description("목표 수행 일정"),
                fieldWithPath("mateSchedule").type(JsonFieldType.STRING).description("팀원의 목표 수행 일정"),
                fieldWithPath("progress").type(JsonFieldType.NUMBER).description("팀원의 목표 수행 진행률")
        );
    }

    private PathParametersSnippet goalIdPathParametersSnippet() {
        return pathParameters(parameterWithName("goalId").description("목표 ID"));
    }

    private SpecifiedGoalDetailInfo getSpecifiedGoalDetailInfo() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        return new SpecifiedGoalDetailInfo(mate,
                List.of(LocalDate.now().minusDays(1), LocalDate.now()),
                List.of(new MateUploadInfo(1L, 2L, LocalDate.now(), "nickname1"),
                        new MateUploadInfo(3L, 4L, LocalDate.now().minusDays(1), "nickname2"))
        );
    }

    private List<GoalHistoryInfo> getGoalHistoryInfoList() {
        Mate mate1 = TestEntityFactory.goal(1L, "goal1")
                .join(TestEntityFactory.user(1L, "user1"));
        Mate mate2 = TestEntityFactory.goal(2L, "goal2")
                .join(TestEntityFactory.user(2L, "user2"));

        return List.of(new GoalHistoryInfo(mate1, List.of("nickname1", "nickname2", "nickname3")),
                new GoalHistoryInfo(mate2, List.of("nickname4", "nickname5")));
    }

}