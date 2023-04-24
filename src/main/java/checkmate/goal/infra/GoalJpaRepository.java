package checkmate.goal.infra;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.GoalStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QVerificationCondition.verificationCondition;


@RequiredArgsConstructor
@Repository
public class GoalJpaRepository implements GoalRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Goal save(Goal goal) {
        entityManager.persist(goal);
        return goal;
    }

    @Override
    public Optional<Goal> findById(long goalId) {
        return Optional.ofNullable(entityManager.find(Goal.class, goalId));
    }

    @Override
    public Optional<Goal> findByIdForUpdate(long goalId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(goal)
                        .where(goal.id.eq(goalId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
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

    public void updateStatusToOver(List<Long> goalIds) {
        jdbcTemplate.batchUpdate("UPDATE goal SET status = ? WHERE id = ?",
                goalIds,
                goalIds.size(),
                (ps, id) -> {
                    ps.setString(1, GoalStatus.OVER.name());
                    ps.setLong(2, id);
                });

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public Optional<Goal> findWithConditions(long goalId) {
        return Optional.ofNullable(
                queryFactory.select(goal).distinct()
                        .from(goal)
                        .leftJoin(goal.conditions, verificationCondition).fetchJoin()
                        .where(goal.id.eq(goalId))
                        .fetchOne()
        );
    }
}