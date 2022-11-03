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
import checkmate.goal.presentation.dto.response.GoalListQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private Goal goal;

    @BeforeEach
    void setUp(){
        goal = TestEntityFactory.goal(1L, "testGoal");
    }

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
        GoalPeriodInfo goalPeriodInfo = goalPeriodResponseDto(goal);
        given(goalQueryService.findGoalPeriodInfo(any(Long.class))).willReturn(goalPeriodInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goal/{goalId}/period", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(goalPeriodInfo)))
                .andDo(document("goal-period",
                        pathParameters(parameterWithName("goalId").description("goalId"))
                ));
    }

    @WithMockAuthUser
    @Test
    void 개별_목표조회_테스트() throws Exception{
        GoalDetailInfo response = getGoalInformationResponse();
        given(goalQueryService.findGoalDetail(any(Long.class), any(Long.class))).willReturn(response);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goal/{goalId}", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("find-goalinfo",
                        pathParameters(parameterWithName("goalId").description("goalId")),
                        setGoalInformationResponseFields()));
    }

    @WithMockAuthUser
    @Test
    void 목표_생성_테스트() throws Exception {
        //given
        GoalCreateDto request = GoalCreateDto.builder()
                .category(GoalCategory.학습).title("자바의 정석 스터디")
                .startDate(LocalDate.of(2021,12,20))
                .endDate(LocalDate.of(2021,12,31))
                .weekDays("월수금")
                .build();
        GoalCreateResult goalCreateResult = new GoalCreateResult(goal.getId());

        //when
        when(goalCommandService.create(any())).thenReturn(goalCreateResult);

        //then
        mockMvc.perform(post("/goal")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(goalCreateResult)))
                .andDo(document("save-goal",
                        setSaveGoalRequestField(),
                        setSaveGoalResponseField()));
    }

    @WithMockAuthUser
    @Test
    void 유저의_진행중인_목표_조회_테스트() throws Exception{
        List<GoalSimpleInfo> goalSimpleInfoList = List.of(simpleGoalInfoResponseDto(goal), simpleGoalInfoResponseDto(goal));

        when(goalQueryService.findOngoingSimpleInfo(any(Long.class))).thenReturn(goalSimpleInfoList);
        GoalListQueryResponse<GoalSimpleInfo> response = new GoalListQueryResponse<>(goalSimpleInfoList);

        mockMvc.perform(get("/goal/ongoing")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("find-userGoal",
                        setGoalFindResponseFields()));
        verify(goalQueryService).findOngoingSimpleInfo(any(Long.class));
    }


    @WithMockAuthUser
    @Test
    void 유저가_오늘해야할_목표_조회_테스트() throws Exception{
        TodayGoalInfo todayGoalInfo1 = getTodayGoalInfo(true);
        TodayGoalInfo todayGoalInfo2 = getTodayGoalInfo(false);

        List<TodayGoalInfo> todayGoalInfoList = List.of(todayGoalInfo1, todayGoalInfo2);
        when(goalQueryService.findTodayGoalInfo(any(Long.class))).thenReturn(todayGoalInfoList);

        GoalListQueryResponse<TodayGoalInfo> response = new GoalListQueryResponse<>(todayGoalInfoList);

        mockMvc.perform(get("/goal/today")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("find-todayGoal",
                        getTodayGoalInfoResponseFieldsSnippet()
                ));
    }

    private TodayGoalInfo getTodayGoalInfo(boolean checked1) {
        return TodayGoalInfo
                .builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .weekDays(goal.getWeekDays())
                .checked(checked1)
                .build();
    }

    @WithMockAuthUser
    @Test
    void 유저의_성공한_목표_목록_조회_테스트() throws Exception{
        TeamMate teamMate = TestEntityFactory.teamMate(1L, 1L);
        goal.addTeamMate(teamMate);

        List<GoalHistoryInfo> goalHistoryInfoList =
                List.of(historyGoalInfoResponseDto(goal, teamMate),
                        historyGoalInfoResponseDto(goal, teamMate));

        given(goalQueryService.findHistoryGoalInfo(any(Long.class))).willReturn(goalHistoryInfoList);

        GoalListQueryResponse<GoalHistoryInfo> response = new GoalListQueryResponse<>(goalHistoryInfoList);

        mockMvc.perform(get("/goal/history")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("goal-history",
                        getHistoryResponseFieldsSnippet()));

    }

    private GoalHistoryInfo historyGoalInfoResponseDto(Goal goal, TeamMate teamMate) {
        return GoalHistoryInfo.builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .achievementRate(teamMate.calcProgressPercent())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .appointmentTime(goal.getAppointmentTime())
                .weekDays(goal.getWeekDays().getKorWeekDay())
                .teamMateNames(getTeamMateNicknameList(goal))
                .build();
    }

    private List<String> getTeamMateNicknameList(Goal goal) {
        return goal.getTeam().stream()
                .map(tm -> tm.getGoal().getTitle() + tm.getId())
                .collect(Collectors.toList());
    }

    private ResponseFieldsSnippet getHistoryResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("goals[].id").description("목표 id").type(JsonFieldType.NUMBER),
                fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("goals[].startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("goals[].endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("goals[].weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("goals[].appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("goals[].achievementRate").type(JsonFieldType.NUMBER).description("유저의 최종 성취율"),
                fieldWithPath("goals[].teamMateNames").type(JsonFieldType.ARRAY).description("팀원들의 닉네임")
        );
    }

    private ResponseFieldsSnippet setGoalFindResponseFields() {
        return responseFields(
                fieldWithPath("goals[].id").description("목표 id").type(JsonFieldType.NUMBER),
                fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("goals[].weekDays").type(JsonFieldType.STRING).description("인증요일")
        );
    }

    private GoalSimpleInfo simpleGoalInfoResponseDto(Goal goal) {
        return GoalSimpleInfo.builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .title(goal.getTitle())
                .weekDays(goal.getWeekDays().getKorWeekDay())
                .build();
    }

    private ResponseFieldsSnippet setSaveGoalResponseField() {
        return responseFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("ID 값")
        );
    }

    private GoalPeriodInfo goalPeriodResponseDto(Goal goal) {
        return GoalPeriodInfo.builder()
                .goalCalendar(goal.getCalendar())
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
                fieldWithPath("weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("minimumLike").type(JsonFieldType.NUMBER).description("확인 후 인증의 최소 좋아요 수").optional()
        );
    }

    private ResponseFieldsSnippet getTodayGoalInfoResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("goals[].id").description("목표 id").type(JsonFieldType.NUMBER),
                fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("goals[].weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("goals[].checked").type(JsonFieldType.BOOLEAN).description("오늘 이미 인증을 수행했는지 여부"));
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
        TeamMate selector = TestEntityFactory.teamMate(1L, 1L);
        goal.addTeamMate(selector);
        goal.addTeamMate(TestEntityFactory.teamMate(2L, 2L));
        return getGoalDetailResponseDto(goal, selector);
    }

    private GoalDetailInfo getGoalDetailResponseDto(Goal goal, TeamMate selector) {
        return GoalDetailInfo.builder()
                .goal(goal)
                .selector(selector)
                .otherTeamMates(getTeamMateResponseList(goal))
                .build();
    }

    private List<TeamMateInfo> getTeamMateResponseList(Goal goal) {
        return goal.getTeam().stream()
                .map(tm -> TeamMateInfo.builder()
                        .teamMate(tm)
                        .nickname("tester")
                        .build())
                .collect(Collectors.toList());
    }

}
