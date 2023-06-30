package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.NotInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalPeriod;
import checkmate.mate.domain.Mate.MateStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class MateTest {

    @Test
    void getAchievementPercentAs100() throws Exception {
        //given
        Mate mate = createMate(create10DaysGoal());
        ReflectionTestUtils.setField(mate.getAttendance(), "checkDayCount", 10);

        //when
        double achievementPercent = mate.getAchievementPercent();

        //then
        assertThat(achievementPercent).isEqualTo(100.0);
    }

    @Test
    void getAchievementPercentAs0() throws Exception {
        //given
        Mate mate = createMate(create10DaysGoal());
        ReflectionTestUtils.setField(mate.getAttendance(), "checkDayCount", 0);

        //when
        double achievementPercent = mate.getAchievementPercent();

        //then
        assertThat(achievementPercent).isZero();
    }

    @Test
    void getAchievementPercentAs50() throws Exception {
        //given
        Mate mate = createMate(create10DaysGoal());
        ReflectionTestUtils.setField(mate.getAttendance(), "checkDayCount", 5);

        //when
        double achievementPercent = mate.getAchievementPercent();

        //then
        assertThat(achievementPercent).isEqualTo(50.0);
    }

    @Test
    void rejectInviteWhenWaitingStatus() throws Exception {
        //given
        Mate mate = createMate(MateStatus.WAITING);

        //when
        mate.rejectInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.REJECT);
    }

    @Test
    void rejectInviteWhenInvalidStatus() throws Exception {
        //given
        Mate mate = createMate(MateStatus.ONGOING);

        //when
        BusinessException exception = assertThrows(BusinessException.class, mate::rejectInvite);

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MATE_STATUS);
    }

    @Test
    void receiveInvite() throws Exception {
        //given
        Mate mate = createMateWithMockGoal(MateStatus.CREATED);

        //when
        mate.receiveInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.WAITING);
        verify(mate.getGoal()).checkInviteable();
    }

    @Test
    void receiveInviteFailBecauseAlreadyInGoal() throws Exception {
        //given
        Mate ongoingMate = createMateWithMockGoal(MateStatus.ONGOING);
        Mate successMate = createMateWithMockGoal(MateStatus.SUCCESS);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> ongoingMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);

        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> successMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);
    }

    @Test
    void receiveInviteFailBecauseAlreadyWaitingStatus() throws Exception {
        //given
        Mate waitingMate = createMateWithMockGoal(MateStatus.WAITING);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> waitingMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATED_INVITE);
    }

    @Test
    void failToWaitingStatusBecauseProgressedGoal() throws Exception {
        //given
        Mate mate = createMateWithNotInviteableGoal(MateStatus.CREATED);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> mate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    @Test
    void changeToOngoingStatus() throws Exception {
        //given
        int progressDays = 5;
        Mate mate = createMateWithMockGoal(MateStatus.WAITING);
        Mockito.when(mate.getGoal().getProgressedCheckDayCount()).thenReturn(progressDays);

        //when
        mate.acceptInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.ONGOING);
        verify(mate.getGoal()).checkInviteable();
        assertThat(mate.getCheckDayCount()).isEqualTo(progressDays);
    }

    @Test
    void acceptInviteFailBecauseStatus() throws Exception {
        //given
        Mate created = createMateWithMockGoal(MateStatus.CREATED);
        Mate rejected = createMateWithMockGoal(MateStatus.REJECT);
        Mate ongoing = createMateWithMockGoal(MateStatus.ONGOING);
        Mate succeeded = createMateWithMockGoal(MateStatus.SUCCESS);

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
    void acceptInviteFailBecauseProgressedGoal() throws Exception {
        //given
        Mate mate = createMateWithNotInviteableGoal(MateStatus.WAITING);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> mate.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    private Mate createMateWithNotInviteableGoal(MateStatus status) {
        Mate mate = createMate(status);
        Goal mockGoal = Mockito.mock(Goal.class);
        ReflectionTestUtils.setField(mate, "goal", mockGoal);
        doAnswer(invocation -> {
            throw NotInviteableGoalException.EXCEED_INVITEABLE_DATE;
        }).when(mockGoal).checkInviteable();
        return mate;
    }

    private Mate createMateWithMockGoal(MateStatus status) {
        Mate mate = createMate(status);
        ReflectionTestUtils.setField(mate, "goal", Mockito.mock(Goal.class));
        return mate;
    }

    private Goal create10DaysGoal() {
        Goal goal = createGoal();
        GoalPeriod period = new GoalPeriod(todayMinusDays(9), today());
        ReflectionTestUtils.setField(goal, "period", period);
        return goal;
    }

    private LocalDate todayMinusDays(int daysToSubtract) {
        return today().minusDays(daysToSubtract);
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private Mate createMate(MateStatus status) {
        Mate mate = createMate(createGoal());
        ReflectionTestUtils.setField(mate, "status", status);
        return mate;
    }

    private Mate createMate(Goal goal) {
        return goal.createMate(TestEntityFactory.user(1L, "user"));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }
}