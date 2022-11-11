package checkmate.goal.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.common.util.WeekDayConverter;
import checkmate.goal.domain.*;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
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

        TeamMate teamMate1 = TestEntityFactory.teamMate(null, user.getId());
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        goal1.addTeamMate(teamMate1);
        TeamMate teamMate2 = TestEntityFactory.teamMate(null, user.getId());
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        goal1.addTeamMate(teamMate2);
        TeamMate teamMate3 = TestEntityFactory.teamMate(null, user.getId());
        ReflectionTestUtils.setField(teamMate3, "status", TeamMateStatus.ONGOING);
        goal1.addTeamMate(teamMate3);

        //when
        int count = goalRepository.countOngoingGoals(user.getId());

        //then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void findConditions() throws Exception{
        //given
        Goal testGoal = TestEntityFactory.goal(null, "testGoal");
        em.persist(testGoal);
        testGoal.addCondition(new LikeCountCondition(5));

        //when
        List<VerificationCondition> conditions = goalRepository.findConditions(testGoal.getId());

        //then
        assertThat(conditions.size()).isEqualTo(1);
    }

    @Test
    void save_VerificationCondition() throws Exception{
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

    @Test
    void findOngoingGoalList() throws Exception{
        //given
        User user = TestEntityFactory.user(null, "ongoingTester2");
        em.persist(user);

        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);

        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        goal.addTeamMate(teamMate);
        teamMate.initiateGoal(0);

        //when
        List<Goal> ongoingGoalList = goalRepository.findOngoingGoalList(user.getId());

        //then
        assertThat(ongoingGoalList.size()).isEqualTo(1);
        assertThat(ongoingGoalList.get(0).getTitle()).contains(goal.getTitle());
    }

    @Test
    void updateYesterdayOveredGoals() throws Exception{
        //given
        Goal testGoal = Goal.builder()
                .title("testGoal")
                .checkDays(WeekDayConverter.convertEngToKor(LocalDate.now().minusDays(2)))
                .category(GoalCategory.ETC)
                .startDate(LocalDate.now().minusDays(6))
                .endDate(LocalDate.now().minusDays(1))
                .build();
        em.persist(testGoal);

        Goal testGoal2 = Goal.builder()
                .title("testGoal2")
                .startDate(LocalDate.now().minusDays(6))
                .endDate(LocalDate.now().minusDays(1))
                .checkDays(WeekDayConverter.convertEngToKor(LocalDate.now().minusDays(2)))
                .category(GoalCategory.ETC)
                .build();
        em.persist(testGoal2);

        Goal oneDayGoal = Goal.builder()
                .title("하루짜리 목표")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().minusDays(1))
                .checkDays(WeekDayConverter.convertEngToKor(LocalDate.now().minusDays(1)))
                .category(GoalCategory.ETC)
                .build();
        em.persist(oneDayGoal);
        em.flush();
        em.clear();

        //when
        List<Goal> goals = goalRepository.updateYesterdayOveredGoals();
        goals.forEach(goal -> assertThat(goal.getGoalStatus()).isEqualTo(GoalStatus.OVER));
    }
}
