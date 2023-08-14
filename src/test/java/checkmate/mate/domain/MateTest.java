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
import org.junit.jupiter.api.Test;
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
    void receiveInvite() throws Exception {
        //given

        Mate mate = createMate(createGoal(), MateStatus.CREATED);

        //when
        mate.receiveInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.WAITING);
    }

    @Test
    void receiveInviteFailBecauseAlreadyInGoal() throws Exception {
        //given
        Mate ongoingMate = createMate(createGoal(), MateStatus.ONGOING);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            ongoingMate::receiveInvite).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);
    }

    @Test
    void receiveInviteFailBecauseAlreadyWaitingStatus() throws Exception {
        //given
        Mate waitingMate = createMate(createGoal(), MateStatus.WAITING);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> waitingMate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATED_INVITE);
    }

    @Test
    void failToWaitingStatusBecauseProgressedGoal() throws Exception {
        //given
        Mate mate = createMate(progressOveredGoal(), MateStatus.CREATED);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            () -> mate.receiveInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    @Test
    void changeToOngoingStatus() throws Exception {
        //given
        Mate mate = createMate(createGoal(), MateStatus.WAITING);

        //when
        mate.acceptInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.ONGOING);
    }

    @Test
    void acceptInviteFailBecauseStatus() throws Exception {
        //given
        Mate created = createMate(createGoal(), MateStatus.CREATED);

        //when //then
        assertThat(assertThrows(BusinessException.class,
            created::acceptInvite).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_MATE_STATUS);
    }

    @Test
    void acceptInviteFailBecauseProgressedGoal() throws Exception {
        //given
        Mate mate = createMate(progressOveredGoal(), MateStatus.WAITING);

        //when //then
        assertThat(assertThrows(NotInviteableGoalException.class,
            mate::acceptInvite).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    private Goal progressOveredGoal() {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", todayMinusDays(100));
        return goal;
    }

    private Mate createMate(Goal goal, MateStatus status) {
        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(mate, "status", status);
        return mate;
    }

    private Goal create10DaysGoal() {
        Goal goal = createGoal();
        GoalPeriod period = new GoalPeriod(todayMinusDays(9), today());
        ReflectionTestUtils.setField(goal, "period", period);
        return goal;
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private LocalDate todayMinusDays(int daysToMinus) {
        return today().minusDays(daysToMinus);
    }

    private Mate createMate(Goal goal) {
        return goal.createMate(TestEntityFactory.user(1L, "user"));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }
}