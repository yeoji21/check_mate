package checkmate.goal.infrastructure;

import checkmate.goal.domain.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.goal.domain.QVerificationCondition.verificationCondition;
import static com.querydsl.core.types.ExpressionUtils.count;


@RequiredArgsConstructor
@Repository
public class GoalJpaRepository implements GoalRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    @Override
    public Goal save(Goal goal) {
        entityManager.persist(goal);
        return goal;
    }

    @Override
    public int countOngoingGoals(long userId) {
        return queryFactory.select(count(goal.id))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .where(teamMate.userId.eq(userId),
                        teamMate.status.eq(TeamMateStatus.ONGOING),
                        goal.status.eq(GoalStatus.ONGOING))
                .fetchOne()
                .intValue();
    }

    @Override
    public Optional<Goal> findByIdForUpdate(long goalId) {
        return Optional.ofNullable(
                queryFactory.select(goal)
                        .from(teamMate)
                        .join(teamMate.goal, goal)
                        .where(goal.id.eq(goalId),
                                teamMate.status.eq(TeamMateStatus.ONGOING)
                                        .or(teamMate.status.eq(TeamMateStatus.SUCCESS)))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne());
    }

    @Override
    public Optional<Goal> findById(long goalId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(goal)
                        .where(goal.id.eq(goalId))
                        .fetchOne()
        );
    }

    @Override
    public boolean checkUserIsInGoal(long goalId, long userId) {
        Integer fetchOne = queryFactory.selectOne()
                .from(teamMate)
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.userId.eq(userId),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetchFirst();
        return fetchOne != null;
    }

    public List<Long> updateYesterdayOveredGoals() {
        List<Goal> yesterdayOveredGoal = queryFactory
                .selectFrom(goal)
                .where(goal.period.endDate.eq(LocalDate.now().minusDays(1)),
                        goal.status.eq(GoalStatus.ONGOING))
                .fetch();

        queryFactory.update(goal)
                .where(goal.in(yesterdayOveredGoal))
                .set(goal.status, GoalStatus.OVER)
                .execute();

        List<Long> goalIds = yesterdayOveredGoal.stream().map(Goal::getId).toList();
        entityManager.flush();
        entityManager.clear();

        return goalIds;
    }

    @Override
    public List<VerificationCondition> findConditions(Long goalId) {
        return queryFactory.selectFrom(verificationCondition)
                .where(verificationCondition.goal.id.eq(goalId))
                .fetch();
    }
}