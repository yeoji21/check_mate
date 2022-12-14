package checkmate.goal.presentation;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.goal.presentation.dto.request.LikeCountCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GoalControllerTest extends ControllerTest {
    @WithMockAuthUser
    @Test
    void ??????_??????_?????????() throws Exception{
        GoalModifyDto request = GoalModifyDto.builder()
                .endDate(LocalDate.of(2022, 5, 30))
                .appointmentTime(LocalTime.now()).build();

        mockMvc.perform(RestDocumentationRequestBuilders
                        .patch("/goal/{goalId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("goal-modify",
                        pathParameters(parameterWithName("goalId").description("????????? ????????? goalId")),
                        requestFields(
                                fieldWithPath("endDate").description("????????? ????????? ?????????"),
                                fieldWithPath("appointmentTime").description("????????? ?????? ??????"),
                                fieldWithPath("timeReset").description("?????? ?????? ?????? ??????")
                        )));
    }

    @WithMockAuthUser
    @Test
    void ?????????_??????_??????_??????_?????????() throws Exception{
        LikeCountCreateDto dto = new LikeCountCreateDto(1L, 5);

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/goal/confirm-like")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf())
        ).andExpect(status().isOk());
        verify(goalCommandService).setLikeCountCondition(any());
    }

    @WithMockAuthUser
    @Test
    void ?????????_??????_?????????_??????() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        GoalScheduleInfo goalScheduleInfo = goalPeriodResponseDto(goal);
        given(goalQueryService.findGoalPeriodInfo(any(Long.class))).willReturn(goalScheduleInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goal/{goalId}/period", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(goalScheduleInfo)))
                .andDo(document("goal-period",
                        pathParameters(parameterWithName("goalId").description("goalId"))
                ));
    }

    @WithMockAuthUser
    @Test
    void ??????_????????????_?????????() throws Exception{
        GoalDetailInfo info = getGoalInformationResponse();
        given(goalQueryService.findGoalDetail(any(Long.class), any(Long.class))).willReturn(info);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goal/{goalId}", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(info)))
                .andDo(document("find-goalinfo",
                        pathParameters(parameterWithName("goalId").description("goalId")),
                        setGoalInformationResponseFields()));
    }

    @WithMockAuthUser
    @Test
    void ??????_??????_?????????() throws Exception {
        //given
        GoalCreateDto request = GoalCreateDto.builder()
                .category(GoalCategory.LEARNING).title("????????? ?????? ?????????")
                .startDate(LocalDate.of(2021,12,20))
                .endDate(LocalDate.of(2021,12,31))
                .appointmentTime(LocalTime.of(19, 30))
                .checkDays("?????????")
                .build();

        //when
        when(goalCommandService.create(any())).thenReturn(1L);

        //then
        mockMvc.perform(post("/goal")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(1L)))
                .andDo(document("save-goal",
                        setSaveGoalRequestField())
                );
    }

    @WithMockAuthUser
    @Test
    void ?????????_????????????_??????_??????_?????????() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        GoalSimpleInfoResult result = new GoalSimpleInfoResult(List.of(simpleGoalInfo(goal), simpleGoalInfo(goal)));

        when(goalQueryService.findOngoingSimpleInfo(any(Long.class))).thenReturn(result);

        mockMvc.perform(get("/goal/ongoing")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("find-userGoal",
                        setGoalFindResponseFields()));
        verify(goalQueryService).findOngoingSimpleInfo(any(Long.class));
    }


    @WithMockAuthUser
    @Test
    void ?????????_???????????????_??????_??????_?????????() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        TodayGoalInfo checked = TodayGoalInfo
                .builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .checkDays(goal.getCheckDays())
                .lastUploadDate(LocalDate.now())
                .build();
        TodayGoalInfo notChecked = TodayGoalInfo
                .builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .checkDays(goal.getCheckDays())
                .lastUploadDate(LocalDate.now().minusDays(1))
                .build();

        TodayGoalInfoResult result = new TodayGoalInfoResult(List.of(checked, notChecked));
        when(goalQueryService.findTodayGoalInfo(any(Long.class))).thenReturn(result);

        mockMvc.perform(get("/goal/today")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("find-todayGoal",
                        todayGoalInfoResponseFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void ?????????_?????????_??????_??????_??????_?????????() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "testGoal");

        List<GoalHistoryInfo> goalHistoryInfoList =
                List.of(historyGoalInfoResponseDto(goal, List.of("nickname")),
                        historyGoalInfoResponseDto(goal, List.of("nickname")));
        GoalHistoryInfoResult result = new GoalHistoryInfoResult(goalHistoryInfoList);

        given(goalQueryService.findHistoryGoalInfo(any(Long.class))).willReturn(result);

        mockMvc.perform(get("/goal/history")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("goal-history",
                        historyResponseFieldsSnippet()));

    }

    private GoalHistoryInfo historyGoalInfoResponseDto(Goal goal, List<String> nicknames) {
        return GoalHistoryInfo.builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .workingDays(10)
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .appointmentTime(goal.getAppointmentTime())
                .checkDays(goal.getCheckDays().intValue())
                .teamMateNames(nicknames)
                .build();
    }

    private ResponseFieldsSnippet historyResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("info[].id").description("?????? id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("info[].startDate").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("info[].endDate").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("info[].checkDays").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("info[].appointmentTime").type(JsonFieldType.STRING).description("?????? ??????").optional(),
                fieldWithPath("info[].achievementRate").type(JsonFieldType.NUMBER).description("????????? ?????? ?????????"),
                fieldWithPath("info[].teamMateNames").type(JsonFieldType.ARRAY).description("???????????? ?????????")
        );
    }

    private ResponseFieldsSnippet setGoalFindResponseFields() {
        return responseFields(
                fieldWithPath("info[].id").description("goal id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("info[].weekDays").type(JsonFieldType.STRING).description("????????????")
        );
    }

    private GoalSimpleInfo simpleGoalInfo(Goal goal) {
        return GoalSimpleInfo.builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .weekDays(goal.getCheckDays().toString())
                .build();
    }

    private GoalScheduleInfo goalPeriodResponseDto(Goal goal) {
        return GoalScheduleInfo.builder()
                .weekDays(goal.getCheckDays().intValue())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .build();
    }

    private RequestFieldsSnippet setSaveGoalRequestField() {
        return requestFields(
                fieldWithPath("category").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("checkDays").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("?????? ??????").optional()
        );
    }

    private ResponseFieldsSnippet todayGoalInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("info[].id").description("?????? id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("info[].checkDays").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("info[].checked").type(JsonFieldType.BOOLEAN).description("?????? ?????? ????????? ??????????????? ??????"));
    }

    private ResponseFieldsSnippet setGoalInformationResponseFields() {
        return responseFields(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("ID ???"),
                fieldWithPath("teamMates").description("????????? ?????? ?????????"),
                fieldWithPath("category").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("weekDays").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("?????? ??????").optional(),
                fieldWithPath("inviteable").type(JsonFieldType.BOOLEAN).description("????????? ??? ?????? ????????????"),
                fieldWithPath("goalStatus").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("teamMates[].id").description("???????????? id"),
                fieldWithPath("teamMates[].userId").description("?????? id"),
                fieldWithPath("teamMates[].nickname").description("????????? ?????????"),
                fieldWithPath("teamMates[].uploaded").description("?????? ??????????????????"),
                fieldWithPath("uploadable.uploaded").description("????????? ????????? ????????? ?????? ??????????????????"),
                fieldWithPath("uploadable.uploadable").description("????????? ????????? ????????? ????????? ???????????? ??? ?????????"),
                fieldWithPath("uploadable.workingDay").description("??????????????? ?????? ?????????"),
                fieldWithPath("uploadable.timeOver").description("?????? ????????? ??????????????????")
        );
    }

    private GoalDetailInfo getGoalInformationResponse() {
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        TeamMate selector = goal.join(TestEntityFactory.user(1L, "user"));
        ReflectionTestUtils.setField(selector, "id", 1L);

        TeamMateUploadInfo teamMateUploadInfo = TeamMateUploadInfo.builder()
                .teamMateId(selector.getId())
                .userId(selector.getUserId())
                .lastUploadDay(LocalDate.now().minusDays(1))
                .nickname("tester")
                .build();

        return GoalDetailInfo.builder()
                .goal(goal)
                .selector(selector)
                .otherTeamMates(List.of(teamMateUploadInfo))
                .build();
    }

}
