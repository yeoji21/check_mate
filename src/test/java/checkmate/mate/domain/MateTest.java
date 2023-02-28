package checkmate.mate.domain;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MateTest {
    @Test
    void 진행률_0_계산_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));

        //when
        double progress = mate.calcProgressPercent();

        //then
        assertThat(progress).isEqualTo(0);
    }

    @Test
    void 업로드_가능_테스트() throws Exception {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));

        Uploadable uploadable = mate.getUploadable();
        assertThat(uploadable.isUploadable()).isTrue();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();

        mate.updateUploadedDate();
        uploadable = mate.getUploadable();
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isTrue();
    }

    @Test
    void 인증시간초과_업로드_불가능_테스트() throws Exception {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));

        assertThat(mate.getUploadable().isTimeOver()).isTrue();
    }

    @Test
    void 인증요일아니라_업로드_불가능_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "checkDays",
                new GoalCheckDays(Collections.singletonList(LocalDate.now().plusDays(1))));
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        assertThat(mate.getUploadable().isWorkingDay()).isFalse();
    }

    @Test
    void 초대응답_거절_테스트() throws Exception {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        mate.toRejectStatus();

        assertThat(mate.getStatus()).isEqualTo(MateStatus.REJECT);
    }
}