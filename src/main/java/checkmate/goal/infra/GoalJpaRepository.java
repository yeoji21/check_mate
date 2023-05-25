package checkmate.goal.infra;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QVerificationCondition.verificationCondition;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.GoalStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


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
    public Optional<Goal> findByIdWithLock(long goalId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(goal)
                .where(goal.id.eq(goalId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne()
        );
    }

    @Override
    public void updateTodayStartStatus() {
        queryFactory.update(goal)
            .where(goal.period.startDate.eq(LocalDate.now()),
                goal.status.eq(GoalStatus.WAITING))
            .set(goal.status, GoalStatus.ONGOING)
            .execute();

        entityManager.flush();
        entityManager.clear();
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