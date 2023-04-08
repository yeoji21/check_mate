package checkmate.goal.infra;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.GoalStatus;
import checkmate.mate.domain.MateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QVerificationCondition.verificationCondition;
import static checkmate.mate.domain.QMate.mate;


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
    public Optional<Goal> findByIdForUpdate(long goalId) {
        return Optional.ofNullable(
                queryFactory.select(goal)
                        .from(mate)
                        .join(mate.goal, goal)
                        .where(goal.id.eq(goalId),
                                mate.status.eq(MateStatus.ONGOING)
                                        .or(mate.status.eq(MateStatus.SUCCESS)))
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
    public void updateTodayStartGoal() {
        queryFactory.update(goal)
                .where(goal.status.eq(GoalStatus.WAITING),
                        goal.period.startDate.eq(LocalDate.now()))
                .set(goal.status, GoalStatus.ONGOING)
                .execute();

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
    public Optional<Goal> findWithConditions(Long goalId) {
        return Optional.ofNullable(
                queryFactory.select(goal).distinct()
                        .from(goal)
                        .leftJoin(goal.conditions, verificationCondition).fetchJoin()
                        .where(goal.id.eq(goalId))
                        .fetchOne()
        );
    }
}