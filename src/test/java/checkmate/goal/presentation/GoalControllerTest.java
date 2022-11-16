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
    void 목표_수정_테스트() throws Exception{
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
                        pathParameters(parameterWithName("goalId").description("수정할 목표의 goalId")),
                        requestFields(
                                fieldWithPath("endDate").description("연장할 목표의 종료일"),
                                fieldWithPath("appointmentTime").description("변경할 인증 시간"),
                                fieldWithPath("timeReset").description("인증 시간 삭제 여부")
                        )));
    }

    @WithMockAuthUser
    @Test
    void 좋아요_확인_조건_할당_테스트() throws Exception{
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
    void 목표의_전체_인증일_조회() throws Exception{
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
    void 개별_목표조회_테스트() throws Exception{
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
    void 목표_생성_테스트() throws Exception {
        //given
        GoalCreateDto request = GoalCreateDto.builder()
                .category(GoalCategory.LEARNING).title("자바의 정석 스터디")
                .startDate(LocalDate.of(2021,12,20))
                .endDate(LocalDate.of(2021,12,31))
                .checkDays("월수금")
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
    void 유저의_진행중인_목표_조회_테스트() throws Exception{
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
    void 유저가_오늘해야할_목표_조회_테스트() throws Exception{
        TodayGoalInfoResult result = new TodayGoalInfoResult(List.of(getTodayGoalInfo(true), getTodayGoalInfo(false)));
        when(goalQueryService.findTodayGoalInfo(any(Long.class))).thenReturn(result);

        mockMvc.perform(get("/goal/today")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("find-todayGoal",
                        todayGoalInfoResponseFieldsSnippet()
                ));
    }

    private TodayGoalInfo getTodayGoalInfo(boolean checked) {
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        return TodayGoalInfo
                .builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .checkDays(goal.getCheckDays())
                .checked(checked)
                .build();
    }

    @WithMockAuthUser
    @Test
    void 유저의_성공한_목표_목록_조회_테스트() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

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
                .weekDays(goal.getCheckDays().intValue())
                .teamMateNames(nicknames)
                .build();
    }

    private ResponseFieldsSnippet historyResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("info[].id").description("목표 id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("info[].startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("info[].endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("info[].weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("info[].appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("info[].achievementRate").type(JsonFieldType.NUMBER).description("유저의 최종 성취율"),
                fieldWithPath("info[].teamMateNames").type(JsonFieldType.ARRAY).description("팀원들의 닉네임")
        );
    }

    private ResponseFieldsSnippet setGoalFindResponseFields() {
        return responseFields(
                fieldWithPath("info[].id").description("goal id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("info[].weekDays").type(JsonFieldType.STRING).description("인증요일")
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
                fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("checkDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("minimumLike").type(JsonFieldType.NUMBER).description("확인 후 인증의 최소 좋아요 수").optional()
        );
    }

    private ResponseFieldsSnippet todayGoalInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("info[].id").description("목표 id").type(JsonFieldType.NUMBER),
                fieldWithPath("info[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("info[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("info[].checkDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("info[].checked").type(JsonFieldType.BOOLEAN).description("오늘 이미 인증을 수행했는지 여부"));
    }

    private ResponseFieldsSnippet setGoalInformationResponseFields() {
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
                fieldWithPath("teamMates[].id").description("팀메이트 id"),
                fieldWithPath("teamMates[].userId").description("유저 id"),
                fieldWithPath("teamMates[].nickname").description("유저의 닉네임"),
                fieldWithPath("teamMates[].uploaded").description("이미 업로드했는지"),
                fieldWithPath("uploadable.uploaded").description("목표를 조회한 유저가 이미 업로드했는지"),
                fieldWithPath("uploadable.uploadable").description("목표를 조회한 유저가 목표를 업로드할 수 있는지"),
                fieldWithPath("uploadable.workingDay").description("업로드하는 날이 맞는지"),
                fieldWithPath("uploadable.timeOver").description("인증 시간이 초과되었는지")
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
