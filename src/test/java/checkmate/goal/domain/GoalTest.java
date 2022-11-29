package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.UnInviteableGoalException;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalTest {
    @Test
    void 인증_시간_경과_테스트() throws Exception{
        Goal timeSetGoal = Goal.builder()
                .category(GoalCategory.ETC)
                .title("title")
                .period(new GoalPeriod(LocalDate.now().minusDays(10L), LocalDate.now().plusDays(30L)))
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .appointmentTime(LocalTime.MIN)
                .build();
        assertThat(timeSetGoal.getAppointmentTime().isBefore(LocalTime.now())).isTrue();
        assertThat(timeSetGoal.isTimeOver()).isTrue();
    }

    @Test
    void 땡땡이_최대치_조회_테스트() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));

        int hookyDayLimit = goal.getHookyDayLimit();
        assertThat(hookyDayLimit).isEqualTo(5);
        assertThat(hookyDayLimit).isLessThan(goal.getSchedule().length());
    }

    @Test
    void 즉시_인증_목표_수정_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        LocalDate endDate = goal.getEndDate();
        GoalModifyDto dto = GoalModifyDto.builder()
                .endDate(endDate.plusDays(10L))
                .appointmentTime(LocalTime.MAX)
                .build();

        //when
        GoalModifyCommand command = GoalModifyCommand.builder()
                .goalId(goal.getId())
                .userId(1L)
                .endDate(dto.getEndDate())
                .appointmentTime(dto.getAppointmentTime())
                .timeReset(false)
                .build();

        int beforePeriodLength = goal.getSchedule().length();
        GoalUpdater goalUpdater = new GoalUpdater(toGoalModifyDto(command));
        goalUpdater.update(goal);
        int afterPeriodLength = goal.getSchedule().length();

        //then
        assertThat(goal.getEndDate()).isAfter(endDate);
        assertThat(goal.getAppointmentTime()).isEqualTo(LocalTime.MAX);
        assertThat(beforePeriodLength + 10).isEqualTo(afterPeriodLength);
    }

    @Test
    void 목표_인증시간_삭제_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");

        GoalModifyCommand command = GoalModifyCommand.builder()
                .timeReset(true)
                .build();

        //when
        GoalUpdater goalUpdater = new GoalUpdater(toGoalModifyDto(command));
        goalUpdater.update(goal);
        //then
        assertThat(goal.getAppointmentTime()).isNull();
    }

    @Test
    void 초대가능한_목표인지_확인_테스트() throws Exception{
        Goal goal = TestEntityFactory.goal(1L, "goal");
        assertThat(goal.isInviteable()).isTrue();
        assertDoesNotThrow(goal::inviteableCheck);
    }

    @Test
    void 초대불가능한_목표_확인_테스트() throws Exception{
        Goal goal = Goal.builder()
                .category(GoalCategory.LEARNING)
                .title("자바의 정석 스터디")
                .period(new GoalPeriod(LocalDate.now().minusDays(200L), LocalDate.now().plusDays(100L)))
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .build();

        assertThat(goal.isInviteable()).isFalse();
        assertThrows(UnInviteableGoalException.class, goal::inviteableCheck);
    }

    @Test
    void 오늘까지_진행된_일_수_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));

        //when
        int futureCount = goal.progressedWorkingDaysCount();

        //then
        assertThat(futureCount).isEqualTo(10);
    }

    @Test
    void 기본_인증_조건_확인() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(teamMate);

        //when
        goal.checkConditions(post);

        //then
        assertThat(post.isChecked()).isTrue();
    }

    @Test
    void 좋아요_개수_인증_조건_확인() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        goal.addCondition(new LikeCountCondition(5));
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(teamMate);

        //when
        goal.checkConditions(post);
        assertThat(post.isChecked()).isFalse();

        //then
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));

        goal.checkConditions(post);
        assertThat(post.isChecked()).isTrue();
    }

    @Test @DisplayName("인증 성공했다가 취소되는 경우")
    void failVerifyConditions() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        goal.addCondition(new LikeCountCondition(3));
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(teamMate);
        ReflectionTestUtils.setField(post, "checked", true);

        //when
        goal.checkConditions(post);

        //then
        assertThat(post.isChecked()).isFalse();
    }

    private GoalModifyRequest toGoalModifyDto(GoalModifyCommand goalModifyCommand){
        return GoalModifyRequest.builder()
                .endDate(goalModifyCommand.endDate())
                .timeReset(goalModifyCommand.timeReset())
                .appointmentTime(goalModifyCommand.appointmentTime())
                .build();
    }
}