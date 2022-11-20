package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.UnInviteableGoalException;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalTest {
    private Goal goal;
    private GoalDtoMapper dtoMapper = GoalDtoMapper.INSTANCE;

    @BeforeEach
    void setUp() {
        goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
    }

    @Test
    void test() throws Exception{
        //given

        //when
        String calendar = goal.getSchedule();
        System.out.println(calendar);

        //then

    }

    @Test
    void 인증_시간_경과_테스트() throws Exception{
        Goal timeSetGoal = Goal.builder()
                .category(GoalCategory.ETC)
                .title("title")
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .appointmentTime(LocalTime.MIN)
                .build();
        assertThat(timeSetGoal.getAppointmentTime().isBefore(LocalTime.now())).isTrue();
        assertThat(timeSetGoal.isTimeOver()).isTrue();
    }

    @Test
    void 땡땡이_최대치_조회_테스트() throws Exception{
        int hookyDayLimit = goal.getHookyDayLimit();
        assertThat(hookyDayLimit).isEqualTo(5);
        assertThat(hookyDayLimit).isLessThan(goal.getSchedule().length());
    }

    private GoalModifyRequest toGoalModifyDto(GoalModifyCommand goalModifyCommand){
        return GoalModifyRequest.builder()
                .endDate(goalModifyCommand.getEndDate())
                .timeReset(goalModifyCommand.isTimeReset())
                .appointmentTime(goalModifyCommand.getAppointmentTime())
                .build();
    }

    @Test
    void 즉시_인증_목표_수정_테스트() throws Exception{
        //given
        goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        LocalDate endDate = goal.getEndDate();
        GoalModifyDto dto = GoalModifyDto.builder()
                .endDate(endDate.plusDays(10L))
                .appointmentTime(LocalTime.MAX)
                .build();

        //when
        GoalModifyCommand command = dtoMapper.toModifyCommand(dto, 1L, 2L);

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
        GoalModifyCommand command = dtoMapper.toModifyCommand(GoalModifyDto.builder().timeReset(true).build(), 1L, 2L);
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
                .startDate(LocalDate.now().minusDays(200L))
                .endDate(LocalDate.now().plusDays(100L))
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .build();

        assertThat(goal.isInviteable()).isFalse();
        assertThrows(UnInviteableGoalException.class, goal::inviteableCheck);
    }

    @Test
    void 오늘까지_진행된_일_수_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");

        //when
        int futureCount = goal.progressedWorkingDaysCount();

        //then
        assertThat(futureCount).isEqualTo(10);
    }

    @Test
    void 기본_인증_조건_확인() throws Exception{
        //given
        Goal testGoal = TestEntityFactory.goal(1L, "test");
        Post post = TestEntityFactory.post(TestEntityFactory.teamMate(1L, 1L));

        //when
        boolean result = testGoal.verifyConditions(post);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 좋아요_개수_인증_조건_확인() throws Exception{
        //given
        Goal testGoal = TestEntityFactory.goal(1L, "test");
        testGoal.addCondition(new LikeCountCondition(5));
        Post post = TestEntityFactory.post(TestEntityFactory.teamMate(1L, 1L));

        //when
        boolean result = testGoal.verifyConditions(post);

        //then
        assertThat(result).isFalse();

        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));

        boolean secondResult = testGoal.verifyConditions(post);
        assertThat(secondResult).isTrue();
    }
}