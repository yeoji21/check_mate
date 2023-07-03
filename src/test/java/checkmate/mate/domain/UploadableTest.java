package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UploadableTest {

    @Test
    @DisplayName("Uploadable 객체 생성 - 업로드 가능")
    void uploadable() throws Exception {
        //given
        Mate mate = createMate(GoalCheckDays.ofKorean("월화수목금토일"), null);

        // when
        Uploadable uploadable = new Uploadable(mate);

        //then
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isCheckDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();
        assertThat(uploadable.isUploadable()).isTrue();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 이미 업로드")
    void uploaded() throws Exception {
        //given
        Mate mate = createMate(GoalCheckDays.ofKorean("월화수목금토일"), null);
        mate.updateLastUpdateDate();

        //when
        Uploadable uploadable = new Uploadable(mate);

        //then
        assertThat(uploadable.isUploaded()).isTrue();
        assertThat(uploadable.isCheckDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();
        assertThat(uploadable.isUploadable()).isFalse();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 인증 시간 초과")
    void uploadableTimeOver() throws Exception {
        //given
        Mate mate = createMate(GoalCheckDays.ofKorean("월화수목금토일"), LocalTime.MIN);

        //when
        Uploadable uploadable = new Uploadable(mate);

        //then
        assertThat(uploadable.isCheckDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isTrue();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isUploadable()).isFalse();
    }

    @Test
    @DisplayName("Uploadable 객체 생성 - 인증 요일이 아님")
    void isNotWorkingDay() throws Exception {
        //given
        Mate mate = createMate(tomorrowCheckDay(), null);

        //when
        Uploadable uploadable = new Uploadable(mate);

        //then
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isCheckDay()).isFalse();
        assertThat(uploadable.isTimeOver()).isFalse();
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }

    private Mate createMate(Goal goal) {
        return goal.createMate(TestEntityFactory.user(1L, "user"));
    }

    private GoalCheckDays tomorrowCheckDay() {
        return GoalCheckDays.ofLocalDates(LocalDate.now().plusDays(1));
    }

    private Mate createMate(GoalCheckDays checkDays, LocalTime appointmentTime) {
        Goal goal = createGoal();
        ReflectionTestUtils.setField(goal, "checkDays", checkDays);
        ReflectionTestUtils.setField(goal, "appointmentTime", appointmentTime);
        return createMate(goal);
    }

}