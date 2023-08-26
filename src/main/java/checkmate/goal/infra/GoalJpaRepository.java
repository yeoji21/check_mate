package checkmate.goal.infra;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QVerificationCondition.verificationCondition;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.VerificationCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
    public void saveCondition(VerificationCondition condition) {
        entityManager.persist(condition);
    }

    @Override
    public Optional<Goal> find(long goalId) {
        return Optional.ofNullable(entityManager.find(Goal.class, goalId));
    }

    @Override
    public Optional<Goal> findForUpdate(long goalId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(goal)
                .where(goal.id.eq(goalId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne()
        );
    }

    @Override
    public List<VerificationCondition> findConditions(long goalId) {
        return queryFactory.select(verificationCondition)
            .from(verificationCondition)
            .where(verificationCondition.goal.id.eq(goalId))
            .fetch();
    }
}