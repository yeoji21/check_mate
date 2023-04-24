package checkmate.goal.infra;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GoalRepositoryTest extends RepositoryTest {
    @Test
    @DisplayName("목표와 조건 함께 조회 - 조건이 존재하는 경우")
    void findConditions() throws Exception {
        //given
        Goal goal = createGoal();
        goal.addCondition(new LikeCountCondition(5));

        //when
        Goal findGoal = goalRepository.findWithConditions(goal.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        List<VerificationCondition> conditions = getGoalConditions(findGoal);
        assertThat(conditions.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("목표 조회 - for update")
    void findByIdForUpdate() throws Exception {
        //given
        Goal goal = createGoal();
        em.flush();
        em.clear();

        //when
        Goal findGoal = goalRepository.findByIdForUpdate(goal.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(findGoal).isEqualTo(goal);
    }

    @Test
    @DisplayName("목표와 조건 함께 조회 - 조건이 없는 경우")
    void findConditionsWithNoConditions() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        Goal findGoal = goalRepository.findWithConditions(goal.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        List<VerificationCondition> conditions = getGoalConditions(findGoal);
        assertThat(conditions.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("목표 인증 조건 추가")
    void addCondition() throws Exception {
        //given
        Goal goal = createGoal();
        LikeCountCondition condition = new LikeCountCondition(5);

        //when
        goal.addCondition(condition);

        //then
        Goal findGoal = goalRepository.findWithConditions(goal.getId())
                .orElseThrow(IllegalArgumentException::new);
        LikeCountCondition findCondition = em.find(LikeCountCondition.class, condition.getId());

        List<VerificationCondition> conditions = getGoalConditions(findGoal);
        assertThat(conditions).contains(findCondition);
        assertThat(findCondition.getMinimumLike()).isEqualTo(5);
    }

    @Test
    @DisplayName("오늘 시작일인 목표의 status 업데이트")
    void updateTodayStartGoal() throws Exception {
        //given
        Goal todayStart1 = createGoal(LocalDate.now(), LocalDate.now().plusDays(10));
        ReflectionTestUtils.setField(todayStart1, "status", GoalStatus.WAITING);

        Goal todayStart2 = createGoal(LocalDate.now(), LocalDate.now().plusDays(10));
        ReflectionTestUtils.setField(todayStart2, "status", GoalStatus.WAITING);

        Goal notToday = createGoal(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));

        //when
        goalRepository.updateTodayStartGoal();

        em.flush();
        em.clear();

        //then
        assertThat(em.find(Goal.class, todayStart1.getId()).getStatus()).isEqualTo(GoalStatus.ONGOING);
        assertThat(em.find(Goal.class, todayStart2.getId()).getStatus()).isEqualTo(GoalStatus.ONGOING);
        assertThat(em.find(Goal.class, notToday.getId()).getStatus()).isEqualTo(GoalStatus.WAITING);
    }

    @Test
    @DisplayName("종료된 목표 status 업데이트")
    void updateYesterdayOveredGoals() throws Exception {
        //given
        createGoal(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1));
        createGoal(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1));
        createGoal(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1));

        //when
        List<Long> overedGoalIds = goalRepository.updateYesterdayOveredGoals();

        List<Goal> goals = em.createQuery("select g from Goal g where g.id in :ids", Goal.class)
                .setParameter("ids", overedGoalIds)
                .getResultList();

        System.out.println(goals.size());
        goals.forEach(goal -> assertThat(goal.getStatus()).isEqualTo(GoalStatus.OVER));
    }

    private Goal createGoal(LocalDate startDate, LocalDate endDate) {
        Goal goal = Goal.builder()
                .title("title")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .category(GoalCategory.ETC)
                .period(new GoalPeriod(startDate, endDate))
                .build();
        em.persist(goal);
        return goal;
    }

    private List<VerificationCondition> getGoalConditions(Goal findGoal) {
        return (List<VerificationCondition>)
                ReflectionTestUtils.getField(findGoal, "conditions");
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        return goal;
    }
}
