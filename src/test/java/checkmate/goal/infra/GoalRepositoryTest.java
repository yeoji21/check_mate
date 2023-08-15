package checkmate.goal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import checkmate.goal.domain.LikeCountCondition;
import checkmate.goal.domain.VerificationCondition;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GoalRepositoryTest extends RepositoryTest {

    @Test
    void findConditions() throws Exception {
        //given
        Goal goal = createGoal();
        goalRepository.saveCondition(new LikeCountCondition(goal, 5));
        em.flush();
        em.clear();

        //when
        List<VerificationCondition> conditions = goalRepository.findConditions(goal.getId());

        //then
        assertThat(conditions.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("목표 조회 - for modify")
    void findByIdForUpdate() throws Exception {
        //given
        Goal goal = createGoal();
        em.flush();
        em.clear();

        //when
        Goal findGoal = goalRepository.findForUpdate(goal.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(findGoal).isEqualTo(goal);
    }

    @Test
    @DisplayName("목표 인증 조건 추가")
    void addCondition() throws Exception {
        //given
        Goal goal = createGoal();
        LikeCountCondition condition = new LikeCountCondition(goal, 5);
        em.flush();
        em.clear();

        //when
        goalRepository.saveCondition(condition);

        //then
        List<VerificationCondition> findCondition = goalRepository.findConditions(goal.getId());
        assertThat(findCondition).contains(condition);
    }

    @Test
    @DisplayName("오늘 시작일인 목표의 status 업데이트")
    void updateTodayStartGoal() throws Exception {
        //given
        Goal todayStart1 = createTodayStartGoal();
        Goal todayStart2 = createTodayStartGoal();
        Goal notToday = createGoal(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
        em.flush();
        em.clear();

        //when
        goalRepository.updateTodayStartGoalsToOngoing();

        //then
        assertThat(em.find(Goal.class, todayStart1.getId()).getStatus()).isEqualTo(
            GoalStatus.ONGOING);
        assertThat(em.find(Goal.class, todayStart2.getId()).getStatus()).isEqualTo(
            GoalStatus.ONGOING);
        assertThat(em.find(Goal.class, notToday.getId()).getStatus()).isEqualTo(GoalStatus.WAITING);
    }

    @Test
    @DisplayName("종료된 목표 status 업데이트")
    void updateYesterdayOveredGoals() throws Exception {
        //given
        Goal goal1 = createGoal(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1));
        Goal goal2 = createGoal(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1));
        Goal goal3 = createGoal(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1));
        List<Long> goalIds = List.of(goal1.getId(), goal2.getId(), goal3.getId());

        //when
        goalRepository.updateStatusToOver(goalIds);

        List<Goal> goals = em.createQuery("select g from Goal g where g.id in :ids", Goal.class)
            .setParameter("ids", goalIds)
            .getResultList();
        assertThat(goals).allMatch(goal -> goal.getStatus() == GoalStatus.OVER);
    }

    private Goal createGoal(LocalDate startDate, LocalDate endDate) {
        Goal goal = Goal.builder()
            .title("title")
            .checkDays(GoalCheckDays.ofKorean("월화수목금토일"))
            .category(GoalCategory.ETC)
            .period(new GoalPeriod(startDate, endDate))
            .build();
        em.persist(goal);
        return goal;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        return goal;
    }

    private Goal createTodayStartGoal() {
        Goal todayStart1 = createGoal(LocalDate.now(), LocalDate.now().plusDays(10));
        ReflectionTestUtils.setField(todayStart1, "status", GoalStatus.WAITING);
        return todayStart1;
    }
}
