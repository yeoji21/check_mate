package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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
                .period(new GoalPeriod(LocalDate.of(2021, 12, 20),
                                        LocalDate.of(2021, 12, 31)))
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
                .period(new GoalPeriod(LocalDate.now().minusDays(10), LocalDate.now().plusDays(20)))
                .checkDays(new GoalCheckDays(Collections.singletonList(LocalDate.now().plusDays(1))))
                .build();
        TeamMate teamMate = new TeamMate(goal, TestEntityFactory.user(1L, "user"));
        assertThat(teamMate.getUploadable().isWorkingDay()).isFalse();
    }

    @Test
    void 초대응답_거절_테스트() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        teamMate.applyInviteReject();

        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.REJECT);
    }
}