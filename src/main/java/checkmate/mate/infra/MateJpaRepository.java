package checkmate.mate.infra;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.MateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.post.domain.QPost.post;

@RequiredArgsConstructor
@Repository
public class MateJpaRepository implements MateRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<Mate> findMateWithGoal(long mateId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(mate)
                        .join(mate.goal, goal).fetchJoin()
                        .where(mate.id.eq(mateId))
                        .fetchOne());
    }

    @Override
    public Optional<Mate> findMateWithGoal(long goalId, long userId) {
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
    public List<Mate> updateYesterdayHookyMates() {
        // bit
//        List<TeamMate> yesterdayTMs = entityManager.createNativeQuery(
//                        "select tm.* from team_mate as tm" +
//                                " join goal as g on g.id = tm.goal_id " +
//                                " where g.check_days & " + (1 << CheckDaysConverter.valueOf(LocalDate.now().minusDays(1).getDayOfWeek().toString()).getValue()) + " != 0" +
//                                " and g.status = 'ONGOING' and tm.status = 'ONGOING'"
//                        , TeamMate.class)
//                .getResultList();

        // in
        List<Mate> yesterdayTMs = entityManager.createNativeQuery(
                        "select m.* from mate as m" +
                                " join goal as g on g.id = m.goal_id " +
                                " where g.check_days in :values" +
                                " and g.status = 'ONGOING' and m.status = 'ONGOING'"
                        , Mate.class)
                .setParameter("values", CheckDaysConverter.matchingDateValues(LocalDate.now().minusDays(1)))
                .getResultList();

        List<Long> checkedTeamMateIds = queryFactory
                .select(post.mate.id)
                .from(post)
                .where(
                        post.mate.in(yesterdayTMs),
                        post.uploadedDate.eq(LocalDate.now()),
                        post.checked.isTrue())
                .fetch();
        yesterdayTMs.removeIf(tm -> checkedTeamMateIds.contains(tm.getId()));

        queryFactory.update(mate)
                .where(mate.in(yesterdayTMs))
                .set(mate.progress.skippedDayCount, mate.progress.skippedDayCount.add(1))
                .execute();

        return yesterdayTMs;
    }

    @Override
    public List<Mate> eliminateOveredMates(List<Mate> hookyTMs) {
        // TODO: 2022/08/25 TM의 hookyCount를 초기에 max로 해놓고 점점 줄이는 방식 고려
        List<Mate> eliminators = hookyTMs.stream()
                .filter(tm -> tm.getHookyDays() >= tm.getGoal().getHookyDayLimit())
                .collect(Collectors.toList());

        queryFactory.update(mate)
                .where(mate.in(eliminators))
                .set(mate.status, MateStatus.OUT)
                .execute();

        return eliminators;
    }

    @Override
    public List<Long> findMateUserIds(Long goalId) {
        return queryFactory
                .select(mate.userId)
                .from(mate)
                .where(mate.goal.id.eq(goalId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetch();
    }

    @Override
    public List<Mate> findMateInGoals(List<Long> goalIds) {
        return queryFactory.select(mate)
                .from(mate)
                .where(mate.goal.id.in(goalIds))
                .fetch();
    }

    @Override
    public boolean isExistMate(long goalId, long userId) {
        Long teamMateId = queryFactory.select(mate.id)
                .from(mate)
                .where(mate.goal.id.eq(goalId),
                        mate.userId.eq(userId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetchOne();
        return teamMateId != null;
    }

    @Override
    public Mate save(Mate mate) {
        entityManager.persist(mate);
        return mate;
    }

}



































