package checkmate.user.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class UserQueryDaoTest extends RepositoryTest {

    @Test
    @DisplayName("유저가 진행 중인 목표 개수")
    void countOngoingGoals() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "ongoingTester");
        em.persist(user);

        Goal goal1 = TestEntityFactory.goal(null, "goal1");
        em.persist(goal1);
        Goal goal2 = TestEntityFactory.goal(null, "goal2");
        em.persist(goal2);
        Goal goal3 = TestEntityFactory.goal(null, "goal3");
        em.persist(goal3);

        Mate mate1 = goal1.join(user);
        ReflectionTestUtils.setField(mate1, "status", MateStatus.ONGOING);
        em.persist(mate1);

        Mate mate2 = goal1.join(user);
        ReflectionTestUtils.setField(mate2, "status", MateStatus.ONGOING);
        em.persist(mate2);

        Mate mate3 = goal1.join(user);
        ReflectionTestUtils.setField(mate3, "status", MateStatus.ONGOING);
        em.persist(mate3);

        //when
        int count = userQueryDao.countOngoingGoals(user.getId());

        //then
        assertThat(count).isEqualTo(3);
    }
}