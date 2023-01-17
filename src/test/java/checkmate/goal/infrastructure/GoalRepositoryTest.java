package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.*;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GoalRepositoryTest extends RepositoryTest {

    @Test @DisplayName("유저가 진행 중인 목표 개수")
    void countOngoingGoals() throws Exception{
        //given
        User user = TestEntityFactory.user(null, "ongoingTester");
        em.persist(user);

        Goal goal1 = TestEntityFactory.goal(null, "goal1");
        em.persist(goal1);
        Goal goal2 = TestEntityFactory.goal(null, "goal2");
        em.persist(goal2);
        Goal goal3 = TestEntityFactory.goal(null, "goal3");
        em.persist(goal3);

        TeamMate teamMate1 = goal1.join(user);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate1);

        TeamMate teamMate2 = goal1.join(user);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate2);

        TeamMate teamMate3 = goal1.join(user);
        ReflectionTestUtils.setField(teamMate3, "status", TeamMateStatus.ONGOING);
        em.persist(teamMate3);

        //when
        int count = goalRepository.countOngoingGoals(user.getId());

        //then
        assertThat(count).isEqualTo(3);
    }

    @Test @DisplayName("목표와 조건 함께 조회 - 조건이 존재하는 경우")
    void findConditions() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        goal.addCondition(new LikeCountCondition(5));
        em.flush();
        em.clear();

        //when
        Goal findGoal = goalRepository.findWithConditions(goal.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        List conditions = (List) ReflectionTestUtils.getField(findGoal, "conditions");
        assertThat(conditions.size()).isEqualTo(1);
    }


    @Test @DisplayName("목표와 조건 함께 조회 - 조건이 없는 경우")
    void findConditionsWithNoConditions() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        em.flush();
        em.clear();

        //when
        Goal findGoal = goalRepository.findWithConditions(goal.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        List conditions = (List) ReflectionTestUtils.getField(findGoal, "conditions");
        assertThat(conditions.size()).isEqualTo(0);
    }

    @Test
    void saveVerificationCondition() throws Exception{
        //given
        Goal testGoal = TestEntityFactory.goal(null, "test");
        em.persist(testGoal);
        LikeCountCondition condition = new LikeCountCondition(5);

        //when
        testGoal.addCondition(condition);
        em.flush();
        em.clear();

        //then
        LikeCountCondition findCondition = em.find(LikeCountCondition.class, condition.getId());
        assertThat(findCondition.getId()).isNotNull();
        assertThat(findCondition.getMinimumLike()).isEqualTo(5);
    }

    @Test @DisplayName("오늘 시작일인 목표의 status 업데이트")
    void updateTodayStartGoal() throws Exception{
        //given
        Goal todayStart1 = Goal.builder()
                .title("todayStart1")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .category(GoalCategory.ETC)
                .period(new GoalPeriod(LocalDate.now(), LocalDate.now().plusDays(10)))
                .build();
        ReflectionTestUtils.setField(todayStart1, "status", GoalStatus.WAITING);
        em.persist(todayStart1);

        Goal todayStart2 = Goal.builder()
                .title("todayStart2")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .category(GoalCategory.ETC)
                .period(new GoalPeriod(LocalDate.now(), LocalDate.now().plusDays(10)))
                .build();
        ReflectionTestUtils.setField(todayStart2, "status", GoalStatus.WAITING);
        em.persist(todayStart2);

        Goal notToday = Goal.builder()
                .title("todayStart2")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .category(GoalCategory.ETC)
                .period(new GoalPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)))
                .build();
        em.persist(notToday);

        //when
        goalRepository.updateTodayStartGoal();

        //then
        assertThat(em.find(Goal.class, todayStart1.getId()).getStatus()).isEqualTo(GoalStatus.ONGOING);
        assertThat(em.find(Goal.class, todayStart2.getId()).getStatus()).isEqualTo(GoalStatus.ONGOING);
        assertThat(em.find(Goal.class, notToday.getId()).getStatus()).isEqualTo(GoalStatus.WAITING);
    }

    @Test
    void updateYesterdayOveredGoals() throws Exception{
        //given
        Goal testGoal = Goal.builder()
                .title("testGoal")
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .category(GoalCategory.ETC)
                .period(new GoalPeriod(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1)))
                .build();
        em.persist(testGoal);

        Goal testGoal2 = Goal.builder()
                .title("testGoal2")
                .period(new GoalPeriod(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1)))
                .checkDays(new GoalCheckDays(Collections.singletonList(LocalDate.now().minusDays(2))))
                .category(GoalCategory.ETC)
                .build();
        em.persist(testGoal2);

        Goal oneDayGoal = Goal.builder()
                .title("하루짜리 목표")
                .period(new GoalPeriod(LocalDate.now().minusDays(1), LocalDate.now().minusDays(1)))
                .checkDays(new GoalCheckDays(Collections.singletonList(LocalDate.now().minusDays(1))))
                .category(GoalCategory.ETC)
                .build();
        em.persist(oneDayGoal);

        em.flush();
        em.clear();

        //when
        List<Long> overedGoalIds = goalRepository.updateYesterdayOveredGoals();
        List<Goal> goals = em.createQuery("select g from Goal g where g.id in :ids", Goal.class)
                .setParameter("ids", overedGoalIds)
                .getResultList();
        goals.forEach(goal -> assertThat(goal.getStatus()).isEqualTo(GoalStatus.OVER));
    }
}
