package checkmate.goal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.LikeCountCondition;
import checkmate.goal.domain.VerificationCondition;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        assertThat(conditions).hasSize(1);
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

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        return goal;
    }
}
