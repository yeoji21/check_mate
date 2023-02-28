package checkmate.goal.presentation;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.presentation.dto.GoalCreateDto;
import checkmate.goal.presentation.dto.GoalCreateResponse;
import checkmate.goal.presentation.dto.GoalModifyDto;
import checkmate.goal.presentation.dto.LikeCountCreateDto;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.domain.Mate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
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
    @DisplayName("목표 생성 API")
    void create() throws Exception {
        long goalId = 1L;
        when(goalCommandService.create(any())).thenReturn(goalId);
        GoalCreateResponse response = new GoalCreateResponse(goalId);

        mockMvc.perform(post("/goal")
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
        LikeCountCreateDto dto = new LikeCountCreateDto(1L, 5);

        mockMvc.perform(RestDocumentationRequestBuilders
                        .post("/goal/like-condition")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(document("goal-like-condition",
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
                        .patch("/goal/{goalId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("goal-modify",
                        modifyPathParametersSnippet(),
                        modifyRequestFieldsSnippet()
                ));
    }

    @WithMockAuthUser
    @Test
    void 목표의_전체_인증일_조회() throws Exception {
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
    void 개별_목표조회_테스트() throws Exception {
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
    void 유저의_진행중인_목표_조회_테스트() throws Exception {
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
    void 유저가_오늘해야할_목표_조회_테스트() throws Exception {
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

    private RequestFieldsSnippet modifyRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("endDate").description("연정된 목표의 종료일"),
                fieldWithPath("timeReset").description("인증 시간 삭제 여부"),
                fieldWithPath("appointmentTime").description("변경할 인증 시간")
        );
    }

    private PathParametersSnippet modifyPathParametersSnippet() {
        return pathParameters(parameterWithName("goalId").description("수정할 목표의 ID"));
    }

    private RequestFieldsSnippet likeConditionRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("목표 ID"),
                fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("최소 좋아요 수")
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
        return responseFields(fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("생성된 목표 ID"));
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

    private RequestFieldsSnippet createRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("checkDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional()
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
                fieldWithPath("mates").description("목표에 속한 팀원들"),
                fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("목표 이름"),
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일"),
                fieldWithPath("weekDays").type(JsonFieldType.STRING).description("인증요일"),
                fieldWithPath("appointmentTime").type(JsonFieldType.STRING).description("인증 시간").optional(),
                fieldWithPath("inviteable").type(JsonFieldType.BOOLEAN).description("초대할 수 있는 목표인지"),
                fieldWithPath("goalStatus").type(JsonFieldType.STRING).description("목표 상태"),
                fieldWithPath("mates[].mateId").description("팀메이트 id"),
                fieldWithPath("mates[].userId").description("유저 id"),
                fieldWithPath("mates[].nickname").description("유저의 닉네임"),
                fieldWithPath("mates[].uploaded").description("이미 업로드했는지"),
                fieldWithPath("uploadable.uploaded").description("목표를 조회한 유저가 이미 업로드했는지"),
                fieldWithPath("uploadable.uploadable").description("목표를 조회한 유저가 목표를 업로드할 수 있는지"),
                fieldWithPath("uploadable.workingDay").description("업로드하는 날이 맞는지"),
                fieldWithPath("uploadable.timeOver").description("인증 시간이 초과되었는지")
        );
    }

    private GoalDetailInfo getGoalInformationResponse() {
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        Mate selector = goal.join(TestEntityFactory.user(1L, "user"));
        ReflectionTestUtils.setField(selector, "id", 1L);

        MateUploadInfo mateUploadInfo = MateUploadInfo.builder()
                .mateId(selector.getId())
                .userId(selector.getUserId())
                .lastUploadDate(LocalDate.now().minusDays(1))
                .nickname("tester")
                .build();
        GoalDetailInfo info = new GoalDetailInfo(goal, selector);
        info.setMates(List.of(mateUploadInfo));
        return info;
    }
}
