package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.NotInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.post.domain.Post;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class GoalTest {

    @Test
    @DisplayName("목표 종료일 업데이트")
    void modifyEndDateSuccess() throws Exception {
        //given
        Goal goal = createGoal();
        GoalModifyEvent modifyEvent = GoalModifyEvent.builder()
            .endDate(goal.getEndDate().plusDays(10))
            .build();

        //when
        goal.modify(modifyEvent);

        //then
        assertThat(goal.getEndDate()).isEqualTo(modifyEvent.getEndDate());
    }

    @Test
    @DisplayName("목표 종료일 업데이트 실패 - 기존 종료일 이전으로 변경할 수 없음")
    void modifyEndDateWhenEarlyEndDate() throws Exception {
        //given
        Goal goal = createGoal();

        //when //then
        assertThat(assertThrows(BusinessException.class,
            () -> goal.modify(GoalModifyEvent.builder()
                .endDate(goal.getEndDate().minusDays(1))
                .build())
        ).getErrorCode()).isEqualTo(ErrorCode.INVALID_GOAL_DATE);
    }

    @Test
    @DisplayName("목표 수행 제한시간 제거 업데이트 성공")
    void modifyRemoveAppointmentTime() throws Exception {
        //given
        Goal goal = createGoalWithAppointmentTime(LocalTime.MIN);

        GoalModifyEvent modifyEvent = GoalModifyEvent.builder()
            .timeReset(true)
            .build();

        //when
        goal.modify(modifyEvent);

        //then
        assertThat(goal.getAppointmentTime()).isNull();
    }

    @Test
    @DisplayName("목표 수행 제한시간 업데이트 성공")
    void modifyAppointmentTime() throws Exception {
        //given
        Goal goal = createGoalWithAppointmentTime(LocalTime.MIN);

        GoalModifyEvent modifyEvent = GoalModifyEvent.builder()
            .appointmentTime(LocalTime.MAX)
            .build();

        //when
        goal.modify(modifyEvent);

        //then
        assertThat(goal.getAppointmentTime()).isEqualTo(modifyEvent.getAppointmentTime());
    }

    @Test
    @DisplayName("목표 업데이트 실패 - 업데이트 가능한 기간이 아닌 경우")
    void modifyFailBecauseModifyDeadline() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "modifiedDateTime", LocalDateTime.now().minusDays(1));

        //when //then
        assertThat(assertThrows(BusinessException.class,
            () -> goal.modify(GoalModifyEvent.builder()
                .appointmentTime(LocalTime.MAX)
                .build())).getErrorCode())
            .isEqualTo(ErrorCode.UPDATE_DURATION);
    }

    @Test
    void isTimeOverTrue() throws Exception {
        //given
        Goal goal = createGoalWithAppointmentTime(LocalTime.MIN);

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isTrue();
        assertThat(goal.getAppointmentTime().isBefore(LocalTime.now())).isTrue();
    }

    @Test
    void isTimeOverFalse() throws Exception {
        //given
        Goal goal = createGoalWithAppointmentTime(LocalTime.MAX);

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isFalse();
        assertThat(goal.getAppointmentTime().isAfter(LocalTime.now())).isTrue();
    }

    @Test
    void isTimeOverWhenEmptyAppointmentTime() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isFalse();
    }

    @Test
    @DisplayName("땡땡이 최대치 조회")
    void getSkippedDayLimit() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "period", new GoalPeriod(
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(10)));

        //when
        int skippedDayLimit = goal.getSkippedDayLimit();

        //then
        assertThat(skippedDayLimit).isEqualTo(3);
    }

    @Test
    @DisplayName("목표 초대 가능 여부 - 초대 가능")
    void isInviteableTrue() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        boolean inviteable = goal.isInviteable();

        // then
        assertThat(inviteable).isTrue();
        assertThat(goal.getPeriod().getProgressedPercent()).isLessThan(
            GoalPolicyConstants.INVITE_MAX_ACCEPTABLE_PERCENT);
    }

    @Test
    @DisplayName("목표 초대 가능 여부 - 초대 불가능")
    void isInviteableFalseBecauseProgressPercent() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            LocalDate.now().minusDays(200L));

        //when
        boolean inviteable = goal.isInviteable();

        // then
        assertThat(inviteable).isFalse();
        assertThat(
            assertThrows(NotInviteableGoalException.class, goal::checkInviteable).getErrorCode()
        ).isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    @Test
    @DisplayName("목표 진행 일 수")
    void getProgressedCheckDayCount() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));
        ReflectionTestUtils.setField(goal, "checkDays", GoalCheckDays.ofKorean("월화수목금토일"));

        //when
        int progressedCheckDayCount = goal.getProgressedCheckDayCount();

        //then
        assertThat(progressedCheckDayCount).isEqualTo(10);
    }

    @Test
    void checkConditionsTrue() throws Exception {
        //given
        Goal goal = createGoal();

        goal.addCondition(createSuccessCondition());
        goal.addCondition(createSuccessCondition());
        goal.addCondition(createSuccessCondition());

        //when
        boolean check = goal.checkConditions(createPost(goal));

        //then
        assertThat(check).isTrue();
    }

    @Test
    void checkConditionsFalse() throws Exception {
        //given
        Goal goal = createGoal();

        goal.addCondition(createFailCondition());
        goal.addCondition(createSuccessCondition());
        goal.addCondition(createSuccessCondition());

        //when
        boolean check = goal.checkConditions(createPost(goal));

        //then
        assertThat(check).isFalse();
    }

    private VerificationCondition createFailCondition() {
        VerificationCondition condition = Mockito.mock(VerificationCondition.class);
        when(condition.satisfy(any(Post.class))).thenReturn(false);
        return condition;
    }

    private VerificationCondition createSuccessCondition() {
        VerificationCondition condition = Mockito.mock(VerificationCondition.class);
        when(condition.satisfy(any(Post.class))).thenReturn(true);
        return condition;
    }

    @Test
    void isTodayCheckDayTrue() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "checkDays", GoalCheckDays.ofKorean("월화수목금토일"));

        //when
        boolean isCheckDay = goal.isTodayCheckDay();

        //then
        assertThat(isCheckDay).isTrue();
    }

    @Test
    void isTodayCheckDayFalse() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "checkDays",
            GoalCheckDays.ofLocalDates(LocalDate.now().minusDays(1)));

        //when
        boolean isCheckDay = goal.isTodayCheckDay();

        //then
        assertThat(isCheckDay).isFalse();
    }

    private Post createPost(Goal goal) {
        return TestEntityFactory.post(goal.createMate(TestEntityFactory.user(1L, "user")));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }

    private Goal createGoalWithAppointmentTime(LocalTime appointmentTime) {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "appointmentTime", appointmentTime);
        return goal;
    }

}