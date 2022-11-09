package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.common.util.WeekDayConverter;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
    void TeamMates_일급_컬렉션_테스트() throws Exception{
        //given
        User user1 = TestEntityFactory.user(1L, "user1");
        TeamMate teamMate1 = TestEntityFactory.teamMate(1L, user1.getId());
        goal.addTeamMate(teamMate1);

        //when
        List<TeamMate> teamMates = goal.getTeam();

        //then
        assertThat(teamMates.size()).isEqualTo(1);
        assertThat(teamMates.get(0).getUserId()).isEqualTo(user1.getId());

        User user2 = TestEntityFactory.user(2L, "user2");
        TeamMate teamMate2 = TestEntityFactory.teamMate(2L, user2.getId());
        assertThrows(UnsupportedOperationException.class, () -> teamMates.add(teamMate2));
    }

    @Test
    void 테스트용_데이터_체크() throws Exception{
        LocalDate startDate = LocalDate.of(2022, 5, 16);
        LocalDate endDate = LocalDate.of(2022, 5, 31);
        String weekDays = "월수금";

        System.out.println(weekDays);

        String[] splitDate = weekDays.split("");
        List<String> engDate = Arrays.stream(splitDate).map(WeekDayConverter::convertKorToEng).collect(Collectors.toList());

        String totalBinaryDate = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    if (engDate.contains(date.getDayOfWeek().toString())) return "1";
                    else return "0";
                }).collect(Collectors.joining());
        System.out.println(totalBinaryDate);
    }

    @Test
    void 인증_시간_경과_테스트() throws Exception{
        Goal timeSetGoal = Goal.builder()
                .category(GoalCategory.기타)
                .title("title")
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .weekDays("월화수목금토일")
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
        assertThat(goal.isInviteable()).isTrue();
    }

    @Test
    void 초대불가능한_목표_확인_테스트() throws Exception{
        goal = Goal.builder()
                .category(GoalCategory.학습)
                .title("자바의 정석 스터디")
                .startDate(LocalDate.now().minusDays(200L))
                .endDate(LocalDate.now().plusDays(100L))
                .weekDays("월화수목금토일")
                .build();

        assertThat(goal.isInviteable()).isFalse();
    }

    @Test
    void 오늘까지_진행된_일_수_테스트() throws Exception{
        //given

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