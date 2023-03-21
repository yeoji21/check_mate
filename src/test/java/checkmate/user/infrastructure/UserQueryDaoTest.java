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
        User user = createUser();

        Goal goal1 = createGoal("goal1");
        Goal goal2 = createGoal("goal2");
        Goal goal3 = createGoal("goal3");
        createMate(user, goal1);
        createMate(user, goal2);
        createMate(user, goal3);

        //when
        int count = userQueryDao.countOngoingGoals(user.getId());

        //then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("닉네임 중복 여부 조회")
    void isExistsNickname() throws Exception {
        //given
        User user = createUser();

        //when
        boolean exist = userQueryDao.isExistsNickname(user.getNickname());
        boolean notExist = userQueryDao.isExistsNickname("notExistNickname");

        //then
        assertThat(exist).isTrue();
        assertThat(notExist).isFalse();
    }

    private void createMate(User user, Goal goal1) {
        Mate mate1 = goal1.join(user);
        ReflectionTestUtils.setField(mate1, "status", MateStatus.ONGOING);
        em.persist(mate1);
    }

    private Goal createGoal(String title) {
        Goal goal = TestEntityFactory.goal(null, title);
        em.persist(goal);
        return goal;
    }

    private User createUser() {
        User user = TestEntityFactory.user(null, "ongoingTester");
        em.persist(user);
        return user;
    }
}