package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.mate.domain.Mate.Uploadable;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MateTest {

    @Test
    @DisplayName("팀원 목표 진행률 계산")
    void calcProgressPercent() throws Exception {
        //given
        Mate mate = createMate();
        Mate progressedMate = createMate();
        ReflectionTestUtils.setField(progressedMate.getProgress(), "checkDayCount", 10);

        //when //then
        assertThat(mate.calcProgressPercent()).isZero();
        assertThat(progressedMate.calcProgressPercent()).isPositive();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 업로드 가능")
    void uploadable() throws Exception {
        //given //when
        Uploadable uploadable = createMate().getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isTrue();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 이미 업로드")
    void uploaded() throws Exception {
        //given
        Mate mate = createMate();
        mate.updatePostUploadedDate();

        //when
        Uploadable uploadable = mate.getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isTrue();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 인증 시간 초과")
    void uploadableTimeOver() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);
        Mate mate = createMate(goal);

        //when
        Uploadable uploadable = mate.getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isTrue();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 인증 요일이 아님")
    void isNotWorkingDay() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "checkDays", tomorrowCheckDay());
        Mate mate = createMate(goal);

        //when
        Uploadable uploadable = mate.getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isFalse();
        assertThat(uploadable.isTimeOver()).isFalse();
    }

    @Test
    @DisplayName("Reject Status로 변경")
    void toRejectStatus() throws Exception {
        //given
        Mate mate = createMate();

        //when
        mate.rejectInvite();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.REJECT);
    }

    @Test
    @DisplayName("WAITING Status로 변경")
    void toWaitingStatus() throws Exception {
        //given
        Mate mate = createMate();

        //when
        mate.receivedInvite();

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
        assertThat(assertThrows(UnInviteableGoalException.class,
            () -> ongoingMate.receivedInvite()).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);
        assertThat(assertThrows(UnInviteableGoalException.class,
            () -> successMate.receivedInvite()).getErrorCode())
            .isEqualTo(ErrorCode.ALREADY_IN_GOAL);
    }

    @Test
    @DisplayName("WAITING Status로 변경 실패 - 이미 Waiting Status")
    void failToWaitingStatusBecauseAlreadyWaitingStatus() throws Exception {
        //given
        Mate waitingMate = createMate(createGoal(), MateStatus.WAITING);

        //when //then
        assertThat(assertThrows(UnInviteableGoalException.class,
            () -> waitingMate.receivedInvite()).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATED_INVITE_REQUEST);
    }

    @Test
    @DisplayName("WAITING Status로 변경 실패 - 목표 진행률 초과")
    void failToWaitingStatusBecauseProgressedGoal() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            LocalDate.now().minusDays(20));

        //when //then
        assertThat(assertThrows(UnInviteableGoalException.class,
            () -> mate.receivedInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_GOAL_INVITEABLE_DATE);
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
            LocalDate.now().minusDays(20));

        //when //then
        assertThat(assertThrows(UnInviteableGoalException.class,
            () -> mate.acceptInvite()).getErrorCode())
            .isEqualTo(ErrorCode.EXCEED_GOAL_INVITEABLE_DATE);
    }

    @Test
    void whenChangeToOngoingStatusSetWorkingDays() throws Exception {
        //given
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            LocalDate.now().minusDays(5));
        Mate mate = createMate(goal, MateStatus.WAITING);

        //when
        mate.acceptInvite();

        //then
        assertThat(mate.getWorkingDays()).isPositive();
        assertThat(mate.getSkippedDays()).isZero();
    }

    private Mate createMate(Goal goal, MateStatus status) {
        Mate mate = createMate(goal);
        ReflectionTestUtils.setField(mate, "status", status);
        return mate;
    }

    private GoalCheckDays tomorrowCheckDay() {
        return new GoalCheckDays(CheckDaysConverter.toKorWeekDay(LocalDate.now().plusDays(1)));
    }

    private Mate createMate(Goal goal) {
        return goal.join(TestEntityFactory.user(1L, "user"));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }

    private Mate createMate() {
        return createMate(createGoal());
    }

    private Mate createWaitingStatusMate() {
        Mate mate = createMate(createGoal());
        mate.receivedInvite();
        return mate;
    }
}