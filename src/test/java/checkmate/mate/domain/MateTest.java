package checkmate.mate.domain;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        return goal.join(TestEntityFactory.user(1L, "user"));
    }
}