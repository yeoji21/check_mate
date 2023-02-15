package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalTest {
    @Test
    @DisplayName("목표 종료일 업데이트 성공")
    void endDate_update() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        LocalDate beforeEndDate = goal.getEndDate();
        GoalModifyRequest request = GoalModifyRequest.builder()
                .endDate(beforeEndDate.plusDays(10))
                .build();

        //when
        goal.update(request);

        //then
        assertThat(beforeEndDate).isNotEqualTo(request.getEndDate());
        assertThat(goal.getEndDate()).isEqualTo(request.getEndDate());
    }

    @Test
    @DisplayName("목표 종료일 업데이트 실패")
    void endDate_update_fail() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        LocalDate beforeEndDate = goal.getEndDate();
        GoalModifyRequest request = GoalModifyRequest.builder()
                .endDate(beforeEndDate.minusDays(10))
                .build();

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> goal.update(request));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GOAL_DATE);
    }

    @Test
    @DisplayName("목표 수행 제한시간 제거 업데이트 성공")
    void timeReset_update() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);

        GoalModifyRequest request = GoalModifyRequest.builder()
                .timeReset(true)
                .build();

        //when
        goal.update(request);

        //then
        assertThat(goal.getAppointmentTime()).isNull();
    }

    @Test
    @DisplayName("목표 수행 제한시간 업데이트 성공")
    void appointmentTime_update() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);
        LocalTime beforeAppointmentTime = goal.getAppointmentTime();

        GoalModifyRequest request = GoalModifyRequest.builder()
                .appointmentTime(LocalTime.MAX)
                .build();

        //when
        goal.update(request);

        //then
        assertThat(beforeAppointmentTime).isNotEqualTo(request.getAppointmentTime());
        assertThat(goal.getAppointmentTime()).isEqualTo(request.getAppointmentTime());
    }

    @Test
    @DisplayName("목표 업데이트 실패 - 업데이트 가능한 기간이 아닌 경우")
    void update_fail() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "modifiedDateTime", LocalDateTime.now().minusDays(1));
        GoalModifyRequest request = GoalModifyRequest.builder()
                .appointmentTime(LocalTime.MAX)
                .build();

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> goal.update(request));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UPDATE_DURATION);
    }

    @Test
    void 인증_시간_경과_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);

        //when
        boolean timeOver = goal.isTimeOver();

        //then
        assertThat(timeOver).isTrue();
        assertThat(goal.getAppointmentTime().isBefore(LocalTime.now())).isTrue();
    }

    @Test
    void 땡땡이_최대치_조회_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));

        //when
        int hookyDayLimit = goal.getHookyDayLimit();

        //then
        assertThat(hookyDayLimit).isEqualTo(5);
        assertThat(hookyDayLimit).isLessThan(goal.getSchedule().length());
    }

    @Test
    void 초대가능한_목표인지_확인_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");

        //when
        boolean inviteable = goal.isInviteable();

        //then
        assertThat(inviteable).isTrue();
        assertDoesNotThrow(goal::inviteableCheck);
    }

    @Test
    void 초대불가능한_목표_확인_테스트() throws Exception {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(200L));

        assertThat(goal.isInviteable()).isFalse();
        UnInviteableGoalException exception = assertThrows(UnInviteableGoalException.class, goal::inviteableCheck);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_INVITEABLE_DATE);
    }

    @Test
    void 오늘까지_진행된_일_수_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));

        //when
        int futureCount = goal.progressedWorkingDaysCount();

        //then
        assertThat(futureCount).isEqualTo(10);
    }

    @Test
    void 기본_인증_조건_확인() throws Exception {
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
    void 좋아요_개수_인증_조건_실패() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        goal.addCondition(new LikeCountCondition(5));
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(teamMate);

        //when
        goal.checkConditions(post);

        //then
        assertThat(post.isChecked()).isFalse();
    }

    @Test
    void 좋아요_개수_인증_조건_성공() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        goal.addCondition(new LikeCountCondition(5));
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(teamMate);
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(1L));

        //when
        goal.checkConditions(post);

        //then
        assertThat(post.isChecked()).isTrue();
    }

    @Test
    @DisplayName("인증 성공했다가 취소되는 경우")
    void failVerifyConditions() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        goal.addCondition(new LikeCountCondition(3));
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = getCheckedPost(teamMate);

        //when
        goal.checkConditions(post);

        //then
        assertThat(post.isChecked()).isFalse();
    }

    private Post getCheckedPost(TeamMate teamMate) {
        Post post = TestEntityFactory.post(teamMate);
        ReflectionTestUtils.setField(post, "checked", true);
        return post;
    }

}