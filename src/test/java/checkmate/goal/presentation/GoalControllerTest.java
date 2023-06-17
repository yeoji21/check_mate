package checkmate.goal.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfoResult;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.application.dto.response.TodayGoalInfoResult;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.presentation.dto.GoalCreateDto;
import checkmate.goal.presentation.dto.GoalCreateResponse;
import checkmate.goal.presentation.dto.GoalModifyDto;
import checkmate.goal.presentation.dto.LikeCountCreateDto;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.domain.Mate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.test.util.ReflectionTestUtils;

public class GoalControllerTest extends ControllerTest {

    @WithMockAuthUser
    @Test
    @DisplayName("목표 생성 API")
    void create() throws Exception {
        long goalId = 1L;
        when(goalCommandService.create(any())).thenReturn(goalId);
        GoalCreateResponse response = new GoalCreateResponse(goalId);

        mockMvc.perform(post("/goals")
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(getGoalCreateDto())))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)))
            .andDo(document("goal-create",
                createRequestFieldsSnippet(),
                createResponseFieldSnippet())
            );
    }

    @WithMockAuthUser
    @Test
    @DisplayName("좋아요 확인 조건 추가 API")
    void addLikeCondition() throws Exception {
        long goalId = 1L;
        LikeCountCreateDto dto = new LikeCountCreateDto(5);

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/goals/{goalId}/like-condition", goalId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()))
            .andExpect(status().isOk())
            .andDo(document("goal-like-condition",
                goalIdPathParametersSnippet(),
                likeConditionRequestFieldsSnippet()
            ));

        verify(goalCommandService).addLikeCountCondition(any());
    }

    @WithMockAuthUser
    @Test
    @DisplayName("목표 수정 API")
    void modify() throws Exception {
        GoalModifyDto request = GoalModifyDto.builder()
            .endDate(LocalDate.of(2022, 5, 30))
            .appointmentTime(LocalTime.now())
            .timeReset(false)
            .build();

        mockMvc.perform(RestDocumentationRequestBuilders
                .patch("/goals/{goalId}", 1L)
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(document("goal-modify",
                goalIdPathParametersSnippet(),
                modifyRequestFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("목표 상세 조회 API")
    void findGoalDetail() throws Exception {
        GoalDetailInfo info = getGoalDetailInfo();
        given(goalQueryService.findGoalDetail(any(Long.class))).willReturn(info);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goals/{goalId}", 1L)
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(info)))
            .andDo(document("goal-detail",
                goalIdPathParametersSnippet(),
                goalDetailResponseFieldsSnippet()));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("목표의 인증일 조회 API")
    void findGoalPeriod() throws Exception {
        GoalScheduleInfo goalScheduleInfo = getGoalScheduleInfo();
        given(goalQueryService.findGoalPeriodInfo(any(Long.class))).willReturn(goalScheduleInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goals/{goalId}/period", 1L)
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(goalScheduleInfo)))
            .andDo(document("goal-period",
                goalIdPathParametersSnippet(),
                periodResponseFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("진행중인 목표 정보 조회 API")
    void findOngoingSimpleInfo() throws Exception {
        OngoingGoalInfoResult result = getOngoingGoalInfoResult();

        when(goalQueryService.findOngoingGoalInfo(any(Long.class))).thenReturn(result);

        mockMvc.perform(get("/goals/ongoing")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("goal-ongoing-info",
                ongoingInfoResponseFieldsSnippet()));
        verify(goalQueryService).findOngoingGoalInfo(any(Long.class));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("오늘 인증해야할 목표 정보 조회 API")
    void findTodayGoalInfo() throws Exception {
        TodayGoalInfoResult result = getTodayGoalInfoResult();
        when(goalQueryService.findTodayGoalInfo(any(Long.class))).thenReturn(result);

        mockMvc.perform(get("/goals/today")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("goal-today-info",
                todayInfoResponseFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("성공한 목표 목록 조회 API")
    void findGoalHistoryResult() throws Exception {
        GoalHistoryInfoResult result = new GoalHistoryInfoResult(createGoalHistoryInfoList());
        given(goalQueryService.findGoalHistoryResult(any(Long.class))).willReturn(result);

        mockMvc.perform(get("/goals/history")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("goal-history",
                historyResultResponseFieldsSnippet())
            );
    }

    private List<GoalHistoryInfo> createGoalHistoryInfoList() {
        Mate mate1 = TestEntityFactory.goal(1L, "goal1")
            .createMate(TestEntityFactory.user(1L, "user1"));
        Mate mate2 = TestEntityFactory.goal(2L, "goal2")
            .createMate(TestEntityFactory.user(2L, "user2"));

        GoalHistoryInfo info1 = new GoalHistoryInfo(mate1);
        info1.setMateNicknames(List.of("nickname1", "nickname2", "nickname3"));
        GoalHistoryInfo info2 = new GoalHistoryInfo(mate2);
        info2.setMateNicknames(List.of("nickname4", "nickname5"));
        return List.of(info1, info2);
    }

    private ResponseFieldsSnippet historyResultResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("goals[].goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
            fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("목표 카테고리"),
            fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
            fieldWithPath("goals[].startDate").type(JsonFieldType.STRING).description("목표 시작일"),
            fieldWithPath("goals[].endDate").type(JsonFieldType.STRING).description("목표 종료일"),
            fieldWithPath("goals[].checkDays").type(JsonFieldType.STRING).description("목표 인증 요일"),
            fieldWithPath("goals[].appointmentTime").type(JsonFieldType.STRING)
                .description("목표 인증 시간").optional(),
            fieldWithPath("goals[].achievementRate").type(JsonFieldType.NUMBER)
                .description("유저의 최종 성취율"),
            fieldWithPath("goals[].mateNicknames").type(JsonFieldType.ARRAY).description("팀원들의 닉네임")
        );
    }

    private ResponseFieldsSnippet todayInfoResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("goals[].id").type(JsonFieldType.NUMBER).description("목표 ID"),
            fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("목표 카테고리"),
            fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
            fieldWithPath("goals[].checkDays").type(JsonFieldType.STRING).description("목표 인증 요일"),
            fieldWithPath("goals[].checked").type(JsonFieldType.BOOLEAN).description("인증 완료 여부"));
    }

    private TodayGoalInfoResult getTodayGoalInfoResult() {
        TodayGoalInfo checked = getTodayGoalInfo(TestEntityFactory.goal(1L, "goal1"));
        ReflectionTestUtils.setField(checked, "checked", true);
        TodayGoalInfo notChecked = getTodayGoalInfo(TestEntityFactory.goal(2L, "goal2"));
        ReflectionTestUtils.setField(notChecked, "checked", false);
        return new TodayGoalInfoResult(List.of(checked, notChecked));
    }

    private TodayGoalInfo getTodayGoalInfo(Goal goal) {
        return TodayGoalInfo
            .builder()
            .id(goal.getId())
            .category(goal.getCategory())
            .title(goal.getTitle())
            .checkDays(goal.getCheckDays())
            .lastUploadDate(LocalDate.now())
            .build();
    }

    private ResponseFieldsSnippet ongoingInfoResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("goals[].id").type(JsonFieldType.NUMBER).description("목표 ID"),
            fieldWithPath("goals[].title").type(JsonFieldType.STRING).description("목표 이름"),
            fieldWithPath("goals[].category").type(JsonFieldType.STRING).description("목표 카테고리"),
            fieldWithPath("goals[].weekDays").type(JsonFieldType.STRING).description("목표 인증 요일")
        );
    }

    private OngoingGoalInfoResult getOngoingGoalInfoResult() {
        return new OngoingGoalInfoResult(List.of(
            simpleGoalInfo(TestEntityFactory.goal(1L, "goal1")),
            simpleGoalInfo(TestEntityFactory.goal(2L, "goal2")))
        );
    }

    private ResponseFieldsSnippet periodResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
            fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
            fieldWithPath("schedule").type(JsonFieldType.STRING)
                .description("인증일 (1이면 인증 요일, 0이면 해당없음)")
        );
    }

    private GoalScheduleInfo getGoalScheduleInfo() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        return GoalScheduleInfo.builder()
            .weekDays(goal.getCheckDays().toInt())
            .startDate(goal.getStartDate())
            .endDate(goal.getEndDate())
            .build();
    }

    private ResponseFieldsSnippet goalDetailResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
            fieldWithPath("category").type(JsonFieldType.STRING).description("목표 카테고리"),
            fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
            fieldWithPath("startDate").type(JsonFieldType.STRING).description("목표 시작일"),
            fieldWithPath("endDate").type(JsonFieldType.STRING).description("목표 종료일"),
            fieldWithPath("weekDays").type(JsonFieldType.STRING).description("목표 인증 요일"),
            fieldWithPath("status").type(JsonFieldType.STRING).description("목표 진행 상태"),
            fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("목표 인증 시간")
                .optional(),
            fieldWithPath("inviteable").type(JsonFieldType.BOOLEAN).description("목표 초대 가능 여부"),
            fieldWithPath("mates").type(JsonFieldType.ARRAY).description("목표에 속한 팀원들"),
            fieldWithPath("mates[].mateId").type(JsonFieldType.NUMBER).description("팀원 ID"),
            fieldWithPath("mates[].userId").type(JsonFieldType.NUMBER).description("유저 ID"),
            fieldWithPath("mates[].nickname").type(JsonFieldType.STRING).description("유저 닉네임"),
            fieldWithPath("mates[].uploaded").type(JsonFieldType.BOOLEAN)
                .description("해당 팀원이 목표를 인증했는지")
        );
    }

    private RequestFieldsSnippet modifyRequestFieldsSnippet() {
        return requestFields(
            fieldWithPath("endDate").description("연정된 목표의 종료일"),
            fieldWithPath("timeReset").description("인증 시간 삭제 여부"),
            fieldWithPath("appointmentTime").description("변경할 인증 시간")
        );
    }

    private PathParametersSnippet goalIdPathParametersSnippet() {
        return pathParameters(parameterWithName("goalId").description("목표 ID"));
    }

    private RequestFieldsSnippet likeConditionRequestFieldsSnippet() {
        return requestFields(
            fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("최소 좋아요 수")
        );
    }

    private GoalCreateDto getGoalCreateDto() {
        return GoalCreateDto.builder()
            .category(GoalCategory.LEARNING)
            .title("자바의 정석 스터디")
            .startDate(LocalDate.of(2021, 12, 20))
            .endDate(LocalDate.of(2021, 12, 31))
            .appointmentTime(LocalTime.of(19, 30))
            .checkDays("월수금")
            .build();
    }

    private ResponseFieldsSnippet createResponseFieldSnippet() {
        return responseFields(
            fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("생성된 목표 ID"));
    }

    private OngoingGoalInfo simpleGoalInfo(Goal goal) {
        return OngoingGoalInfo.builder()
            .id(goal.getId())
            .category(goal.getCategory())
            .title(goal.getTitle())
            .weekDays(goal.getCheckDays().toInt())
            .build();
    }

    private RequestFieldsSnippet createRequestFieldsSnippet() {
        return requestFields(
            fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
            fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
            fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
            fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
            fieldWithPath("checkDays").type(JsonFieldType.STRING).description("인증요일"),
            fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간")
                .optional()
        );
    }

    private GoalDetailInfo getGoalDetailInfo() {
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        GoalDetailInfo info = new GoalDetailInfo(goal);
        info.setMates(List.of(
            MateUploadInfo.builder()
                .mateId(1L)
                .userId(2L)
                .lastUploadDate(LocalDate.now().minusDays(1))
                .nickname("tester1")
                .build(),
            MateUploadInfo.builder()
                .mateId(3L)
                .userId(4L)
                .lastUploadDate(LocalDate.now())
                .nickname("tester2")
                .build()));
        return info;
    }
}
