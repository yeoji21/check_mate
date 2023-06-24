package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.NotInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
import checkmate.post.domain.Post;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GoalTest {

    @Test
    @DisplayName("목표 종료일 업데이트")
    void endDate_update() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        GoalModifyEvent request = getEndDateModifyRequest(goal);
        //when
        LocalDate beforeEndDate = goal.getEndDate();
        goal.modify(request);
        //then
        assertThat(beforeEndDate).isNotEqualTo(request.getEndDate());
        assertThat(goal.getEndDate()).isEqualTo(request.getEndDate());
    }

    @Test
    @DisplayName("목표 종료일 업데이트 실패 - 기존 종료일 이전으로 변경할 수 없음")
    void endDate_update_fail() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> goal.modify(GoalModifyEvent.builder()
                .endDate(goal.getEndDate().minusDays(1))
                .build())
        );
        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GOAL_DATE);
    }

    @Test
    @DisplayName("목표 수행 제한시간 제거 업데이트 성공")
    void timeReset_update() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);
        GoalModifyEvent request = GoalModifyEvent.builder()
            .timeReset(true)
            .build();
        //when
        goal.modify(request);
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
        GoalModifyEvent request = GoalModifyEvent.builder()
            .appointmentTime(LocalTime.MAX)
            .build();
        //when
        goal.modify(request);

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
        GoalModifyEvent request = GoalModifyEvent.builder()
            .appointmentTime(LocalTime.MAX)
            .build();
        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> goal.modify(request));
        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UPDATE_DURATION);
    }

    @Test
    @DisplayName("인증 시간 경과")
    void isTimeOver() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal, "appointmentTime", LocalTime.MIN);
        //when
        boolean timeOver = goal.isTimeOver();
        //then
        assertThat(timeOver).isTrue();
        assertThat(goal.getAppointmentTime().isBefore(LocalTime.now())).isTrue();
    }

    // TODO: 2023/06/25 개선
    @Test
    @DisplayName("땡땡이 최대치 조회")
    void getHookyDayLimit() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));

        //when
        int skippedDayLimit = goal.getSkippedDayLimit();

        //then
        assertThat(skippedDayLimit).isEqualTo(5);
    }

    @Test
    @DisplayName("목표 초대 가능 여부 - 초대 가능")
    void isInviteable_true() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");

        //when //then
        assertThat(goal.isInviteable()).isTrue();
        assertDoesNotThrow(goal::checkInviteable);
    }

    @Test
    @DisplayName("목표 초대 가능 여부 - 초대 불가능")
    void isInviteable_false() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate",
            LocalDate.now().minusDays(200L));

        //when //then
        assertThat(goal.isInviteable()).isFalse();
        NotInviteableGoalException exception = assertThrows(NotInviteableGoalException.class,
            goal::checkInviteable);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_INVITEABLE_DATE);
    }

    @Test
    @DisplayName("목표 진행 일 수")
    void progressedWorkingDaysCount() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));
        //when
        int futureCount = goal.getProgressedCheckDayCount();
        //then
        assertThat(futureCount).isEqualTo(10);
    }

    @Test
    @DisplayName("기본 인증 조건 검사")
    void checkConditions() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        Post post = TestEntityFactory.post(goal.createMate(TestEntityFactory.user(1L, "user")));

        //when
        boolean check = goal.checkConditions(post);

        //then
        assertThat(check).isTrue();
    }

    @Test
    @DisplayName("좋아요 개수 인증 조건 검사 - 성공")
    void checkLikeConditions_success() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        goal.addCondition(new LikeCountCondition(5));
        Post post = TestEntityFactory.post(goal.createMate(TestEntityFactory.user(1L, "user")));
        for (int i = 0; i < 5; i++) {
            post.addLikes(i);
        }
        //when

        boolean check = goal.checkConditions(post);

        //then
        assertThat(check).isTrue();
    }

    @Test
    @DisplayName("좋아요 개수 인증 조건 검사 - 실패")
    void checkLikeConditions_fail() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        goal.addCondition(new LikeCountCondition(5));
        Post post = TestEntityFactory.post(goal.createMate(TestEntityFactory.user(1L, "user")));
        //when
        goal.checkConditions(post);
        //then
        assertThat(post.isChecked()).isFalse();
    }

    @Test
    @DisplayName("좋아요 개수 인증 조건 검사 - 성공 후 실패")
    void checkLikeConditions_fail_after_success() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        goal.addCondition(new LikeCountCondition(3));
        Post post = getCheckedPost(goal.createMate(TestEntityFactory.user(1L, "user")));
        ReflectionTestUtils.setField(post, "checked", true);
        //when
        boolean check = goal.checkConditions(post);
        //then
        assertThat(check).isFalse();
    }

    private GoalModifyEvent getEndDateModifyRequest(Goal goal) {
        return GoalModifyEvent.builder()
            .endDate(goal.getEndDate().plusDays(10))
            .build();
    }

    private Post getCheckedPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        ReflectionTestUtils.setField(post, "checked", true);
        return post;
    }

}