package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.NotInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalPeriod;
import checkmate.mate.domain.Mate.MateStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MateTest {

    @Test
    void getAchievementPercentAs100() throws Exception {
        //given
        Goal goal = createGoal();
        GoalPeriod period = new GoalPeriod(todayMinusDays(9), today());
        ReflectionTestUtils.setField(goal, "period", period);

        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(mate.getAttendance(), "checkDayCount", 10);

        //when
        double achievementPercent = mate.getAchievementPercent();

        //then
        assertThat(achievementPercent).isEqualTo(100.0);
    }

    @Test
    void getAchievementPercentAs0() throws Exception {
        //given
        Goal goal = createGoal();
        GoalPeriod period = new GoalPeriod(todayMinusDays(9), today());
        ReflectionTestUtils.setField(goal, "period", period);

        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(mate.getAttendance(), "checkDayCount", 0);

        //when
        double achievementPercent = mate.getAchievementPercent();

        //then
        assertThat(achievementPercent).isZero();
    }

    @Test
    void getAchievementPercentAs50() throws Exception {
        //given
        Goal goal = createGoal();
        GoalPeriod period = new GoalPeriod(todayMinusDays(9), today());
        ReflectionTestUtils.setField(goal, "period", period);

        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(mate.getAttendance(), "checkDayCount", 5);

        //when
        double achievementPercent = mate.getAchievementPercent();

        //then
        assertThat(achievementPercent).isEqualTo(50.0);
    }

    @Test
    void rejectInviteWhenWaitingStatus() throws Exception {
        //given
        Mate mate = createMate(createGoal(), MateStatus.WAITING);

        //when
        mate.rejectInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.REJECT);
    }

    @Test
    void rejectInviteWhenInvalidStatus() throws Exception {
        //given
        Mate mate = createMate(createGoal(), MateStatus.ONGOING);

        //when
        BusinessException exception = assertThrows(BusinessException.class, mate::rejectInvite);

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MATE_STATUS);
    }

    @Test
    @DisplayName("WAITING Status로 변경")
    void toWaitingStatus() throws Exception {
        //given
        Mate mate = createMate();

        //when
        mate.receiveInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.WAITING);
    }

    @Test
    @DisplayName("WAITING Status로 변경 실패 - 이미 목표에 속한 팀원")
    void failToWaitingStatusBecauseAlreadyInGoal() throws Exception {
        //given
        Mate ongoingMate = createMate(createGoal(), MateStatus.ONGOING);
        Mate successMate = createMate(createGoal(), MateStatus.SUCCESS);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> ongoingMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> successMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);
    }

    @Test
    @DisplayName("WAITING Status로 변경 실패 - 이미 Waiting Status")
    void failToWaitingStatusBecauseAlreadyWaitingStatus() throws Exception {
        //given
        Mate waitingMate = createMate(createGoal(), MateStatus.WAITING);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> waitingMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATED_INVITE);
    }

    @Test
    @DisplayName("WAITING Status로 변경 실패 - 목표 진행률 초과")
    void failToWaitingStatusBecauseProgressedGoal() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            todayMinusDays(20));

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> mate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    @Test
    @DisplayName("ONGOING Status로 변경")
    void changeToOngoingStatus() throws Exception {
        //given
        Mate mate = createMate(createGoal(), MateStatus.WAITING);

        //when
        mate.acceptInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.ONGOING);
    }

    @Test
    @DisplayName("ONGOING Status로 변경 실패")
    void failToOngoingStatusBecauseStatus() throws Exception {
        //given
        Mate created = createMate(createGoal(), MateStatus.CREATED);
        Mate rejected = createMate(createGoal(), MateStatus.REJECT);
        Mate ongoing = createMate(createGoal(), MateStatus.ONGOING);
        Mate succeeded = createMate(createGoal(), MateStatus.SUCCESS);

        //when //then
        assertThat(assertThrows(BusinessException.class,
            () -> created.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_MATE_STATUS);
        assertThat(assertThrows(BusinessException.class,
            () -> rejected.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_MATE_STATUS);
        assertThat(assertThrows(BusinessException.class,
            () -> ongoing.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_MATE_STATUS);
        assertThat(assertThrows(BusinessException.class,
            () -> succeeded.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_MATE_STATUS);
    }

    @Test
    void failToOngoingStatusBecauseProgressedGoal() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            todayMinusDays(20));

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> mate.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    @Test
    void whenChangeToOngoingStatusSetWorkingDays() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            todayMinusDays(5));
        Mate mate = createMate(goal, MateStatus.WAITING);

        //when
        mate.acceptInvite();

        //then
        assertThat(mate.getCheckDayCount()).isPositive();
        assertThat(mate.getSkippedDayCount()).isZero();
    }

    private LocalDate todayMinusDays(int daysToSubtract) {
        return today().minusDays(daysToSubtract);
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private Mate createMate(Goal goal, MateStatus status) {
        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(mate, "status", status);
        return mate;
    }

    private Mate createMate(Goal goal) {
        return goal.createMate(TestEntityFactory.user(1L, "user"));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }

    private Mate createMate() {
        return createMate(createGoal());
    }
}