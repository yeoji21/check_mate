package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.ExceedGoalLimitException;
import checkmate.exception.UnInviteableGoalException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamMateTest {
    @Test
    void 진행률_0_계산_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

        //when
        double progress = teamMate.calcProgressPercent();

        //then
        assertThat(progress).isEqualTo(0);
    }

    @Test
    void 초대_수락시_진행중인_목표개수_검사_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

        //when then
        assertThrows(ExceedGoalLimitException.class,
                () -> teamMate.initiateGoal(11));
    }

    @Test
    void 업로드_가능_테스트() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

        Uploadable uploadable = teamMate.getUploadable();
        assertThat(uploadable.isUploadable()).isTrue();
        assertThat(uploadable.isUploaded()).isFalse();
        assertThat(uploadable.isWorkingDay()).isTrue();
        assertThat(uploadable.isTimeOver()).isFalse();

        teamMate.updateUploadedDate();
        uploadable = teamMate.getUploadable();
        assertThat(uploadable.isUploadable()).isFalse();
        assertThat(uploadable.isUploaded()).isTrue();
    }

    @Test
    void 인증시간초과_업로드_불가능_테스트() throws Exception{
        Goal goal = Goal.builder()
                .category(GoalCategory.LEARNING)
                .title("자바의 정석 스터디")
                .startDate(LocalDate.of(2021, 12, 20))
                .endDate(LocalDate.of(2021, 12, 31))
                .appointmentTime(LocalTime.MIN)
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .build();
        TeamMate teamMate = new TeamMate(goal, TestEntityFactory.user(1L, "user"));

        assertThat(teamMate.getUploadable().isTimeOver()).isTrue();
    }

    @Test
    void 인증요일아니라_업로드_불가능_테스트() throws Exception{
        //given
        Goal goal = Goal.builder()
                .category(GoalCategory.LEARNING)
                .title("자바의 정석 스터디")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .checkDays(new GoalCheckDays(String.valueOf(LocalDate.now().plusDays(1))))
                .build();
        TeamMate teamMate = new TeamMate(goal, TestEntityFactory.user(1L, "user"));
        assertThat(teamMate.getUploadable().isWorkingDay()).isFalse();
    }

    @Test
    void 초대응답_수락_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

        int before = teamMate.getWorkingDays();

        //when
        teamMate.initiateGoal(0);
        int after = teamMate.getWorkingDays();

        //then
        assertThat(after).isGreaterThan(before);
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.ONGOING);
    }

    @Test
    void 초대응답_거절_테스트() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        teamMate.applyInviteReject();

        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.REJECT);
    }

    @Test
    void 기간만료된_초대응답_테스트() throws Exception{
        Goal goal = Goal.builder()
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(1))
                .build();
        TeamMate teamMate = new TeamMate(goal, TestEntityFactory.user(1L, "user"));

        assertThrows(UnInviteableGoalException.class, () -> teamMate.initiateGoal(0));
    }
}