package checkmate.goal.infrastructure;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.goal.domain.TeamMateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.post.domain.QPost.post;

@RequiredArgsConstructor
@Repository
public class TeamMateJpaRepository implements TeamMateRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<TeamMate> findTeamMateWithGoal(long teamMateId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(teamMate)
                        .join(teamMate.goal, goal).fetchJoin()
                        .where(teamMate.id.eq(teamMateId))
                        .fetchOne());
    }

    @Override
    public Optional<TeamMate> findTeamMateWithGoal(long goalId, long userId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(teamMate)
                        .where(teamMate.goal.id.eq(goalId),
                                teamMate.userId.eq(userId))
                        .fetchOne()
        );
    }

    @Override
    public List<TeamMate> updateYesterdayHookyTMs() {
        // bit
//        List<TeamMate> yesterdayTMs = entityManager.createNativeQuery(
//                        "select tm.* from team_mate as tm" +
//                                " join goal as g on g.id = tm.goal_id " +
//                                " where g.check_days & " + (1 << CheckDaysConverter.valueOf(LocalDate.now().minusDays(1).getDayOfWeek().toString()).getValue()) + " != 0" +
//                                " and g.status = 'ONGOING' and tm.status = 'ONGOING'"
//                        , TeamMate.class)
//                .getResultList();

        // in
        List<TeamMate> yesterdayTMs = entityManager.createNativeQuery(
                        "select tm.* from team_mate as tm" +
                                " join goal as g on g.id = tm.goal_id " +
                                " where g.check_days in :values" +
                                " and g.status = 'ONGOING' and tm.status = 'ONGOING'"
                        , TeamMate.class)
                .setParameter("values", CheckDaysConverter.matchingDateValues(LocalDate.now().minusDays(1)))
                .getResultList();

        List<Long> checkedTeamMateIds = queryFactory
                .select(post.teamMate.id)
                .from(post)
                .where(
                        post.teamMate.in(yesterdayTMs),
                        post.uploadedDate.eq(LocalDate.now()),
                        post.checked.isTrue())
                .fetch();
        yesterdayTMs.removeIf(tm -> checkedTeamMateIds.contains(tm.getId()));

        queryFactory.update(teamMate)
                .where(teamMate.in(yesterdayTMs))
                .set(teamMate.progress.skippedDayCount, teamMate.progress.skippedDayCount.add(1))
                .execute();

        return yesterdayTMs;
    }

    @Override
    public List<TeamMate> eliminateOveredTMs(List<TeamMate> hookyTMs) {
        // TODO: 2022/08/25 TM??? hookyCount??? ????????? max??? ????????? ?????? ????????? ?????? ??????
        List<TeamMate> eliminators = hookyTMs.stream()
                .filter(tm -> tm.getHookyDays() >= tm.getGoal().getHookyDayLimit())
                .collect(Collectors.toList());

        queryFactory.update(teamMate)
                .where(teamMate.in(eliminators))
                .set(teamMate.status, TeamMateStatus.OUT)
                .execute();

        return eliminators;
    }

    @Override
    public List<Long> findTeamMateUserIds(Long goalId) {
        return queryFactory
                .select(teamMate.userId)
                .from(teamMate)
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetch();
    }

    @Override
    public List<TeamMate> findTeamMates(List<Long> goalIds) {
        return queryFactory.select(teamMate)
                .from(teamMate)
                .where(teamMate.goal.id.in(goalIds))
                .fetch();
    }

    @Override
    public TeamMate save(TeamMate teamMate) {
        entityManager.persist(teamMate);
        return teamMate;
    }

}



































