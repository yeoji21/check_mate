package checkmate.mate.infra;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.post.domain.QPost.post;
import static com.querydsl.core.types.ExpressionUtils.count;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.OngoingGoalCount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MateJpaRepository implements MateRepository {

    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    @Override
    public Optional<Mate> findById(long mateId) {
        return Optional.ofNullable(entityManager.find(Mate.class, mateId));
    }

    @Override
    public Optional<Mate> findWithGoal(long mateId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(mate)
                .join(mate.goal, goal).fetchJoin()
                .where(mate.id.eq(mateId))
                .fetchOne()
        );
    }

    @Override
    public Optional<Mate> findWithGoal(long goalId, long userId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(mate)
                .join(mate.goal, goal).fetchJoin()
                .where(mate.goal.id.eq(goalId),
                    mate.userId.eq(userId))
                .fetchOne()
        );
    }

    @Override
    public List<Mate> findYesterdaySkippedMates() {
        LocalDate yesterDay = LocalDate.now().minusDays(1);
        List<Mate> mates = queryFactory.select(mate)
            .from(mate)
            .where(
                mate.status.eq(MateStatus.ONGOING),
                goal.status.eq(GoalStatus.ONGOING),
                goal.checkDays.checkDays.in(
                    CheckDaysConverter.getAllPossibleValues(yesterDay.getDayOfWeek()))
            )
            .fetch();

        List<Long> uploadedMateIds = queryFactory
            .select(post.mate.id)
            .from(post)
            .where(
                post.mate.in(mates),
                post.createdDate.eq(yesterDay),
                post.checked.isTrue())
            .fetch();

        mates.removeIf(tm -> uploadedMateIds.contains(tm.getId()));
        return mates;
    }

    @Override
    public List<Mate> findAllWithGoal(List<Long> mateIds) {
        return queryFactory.selectFrom(mate)
            .join(mate.goal, goal).fetchJoin()
            .where(mate.id.in(mateIds))
            .fetch();
    }

    @Override
    public OngoingGoalCount findOngoingCount(long userId) {
        int count = queryFactory.select(mate.id)
            .from(mate)
            .where(mate.userId.eq(userId),
                mate.status.eq(MateStatus.ONGOING))
            .fetch()
            .size();
        return new OngoingGoalCount(count);
    }

    @Override
    public void increaseSkippedDayCount(List<Mate> mates) {
        jdbcTemplate.batchUpdate(
            "update mate set skipped_day_count = skipped_day_count + 1 where id = ?",
            mates,
            mates.size(),
            (ps, mate) -> ps.setLong(1, mate.getId())
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void updateLimitOveredMates(List<Mate> limitOveredMates) {
        jdbcTemplate.batchUpdate(
            "update mate set status = 'OUT' where id = ?",
            limitOveredMates,
            limitOveredMates.size(),
            (ps, mate) -> ps.setLong(1, mate.getId())
        );
    }

    @Override
    public Mate save(Mate mate) {
        entityManager.persist(mate);
        return mate;
    }

    private int selectOngoingGoalCount(Mate findMate) {
        return queryFactory.select(count(mate.id))
            .from(mate)
            .where(mate.userId.eq(findMate.getUserId()),
                mate.status.eq(MateStatus.ONGOING))
            .fetchOne()
            .intValue();
    }
}



































