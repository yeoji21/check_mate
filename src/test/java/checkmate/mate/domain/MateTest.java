package checkmate.mate.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MateTest {
    @Test
    @DisplayName("팀원 목표 진행률 계산")
    void calcProgressPercent() throws Exception {
        //given
        Mate createdMate = createMate();
        Mate progressedMate = createMate();
        ReflectionTestUtils.setField(progressedMate.getProgress(), "checkDayCount", 10);

        //when then
        assertThat(createdMate.calcProgressPercent()).isEqualTo(0);
        assertThat(progressedMate.calcProgressPercent()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 업로드 가능")
    void getUploadable_uploadable() throws Exception {
        //given
        Mate mate = createMate();

        //when
        Uploadable uploadable = mate.getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isTrue();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 이미 업로드")
    void getUploadable_uploaded() throws Exception {
        //given
        Mate mate = createMate();
        mate.updateUploadedDate();

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
    void getUploadable_time_over() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));

        //when
        Uploadable uploadable = mate.getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(mate.getUploadable().isTimeOver()).isTrue();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 인증 요일이 아님")
    void getUploadable_not_working_day() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "checkDays",
                new GoalCheckDays(Collections.singletonList(LocalDate.now().plusDays(1))));
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));

        //when
        Uploadable uploadable = mate.getUploadable();

        //then
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(mate.getUploadable().isWorkingDay()).isFalse();
        assertThat(mate.getUploadable().isTimeOver()).isFalse();
    }

    @Test
    @DisplayName("초대 응답 거절")
    void toRejectStatus() throws Exception {
        Mate mate = createMate();
        mate.toRejectStatus();

        assertThat(mate.getStatus()).isEqualTo(MateStatus.REJECT);
    }

    @Test
    @DisplayName("ONGOING 상태로 변경 실패 - status")
    void toOngoingStatus_fail_status() throws Exception {
        //given
        Mate created = createMate();

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> created.toOngoingStatus());

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MATE_STATUS);
    }

    @Test
    @DisplayName("ONGOING 상태로 변경 성공")
    void toOngoingStatus_success() throws Exception {
        //given
        Mate mate = createWaitingMate();

        //when
        mate.toOngoingStatus();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.ONGOING);
        assertThat(mate.calcProgressPercent()).isGreaterThan(0);
    }

    @Test
    @DisplayName("WAITING 상태로 변경")
    void toWaitingStatus() throws Exception {
        //given
        Mate mate = createMate();

        //when
        mate.toWaitingStatus();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.WAITING);
    }

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        return goal.join(TestEntityFactory.user(1L, "user"));
    }

    private Mate createWaitingMate() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
                LocalDate.now().minusDays(10));
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        mate.toWaitingStatus();
        return mate;
    }
}