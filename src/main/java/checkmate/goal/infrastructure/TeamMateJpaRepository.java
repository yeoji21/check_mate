package checkmate.goal.infrastructure;

import checkmate.common.util.WeekDayConverter;
import checkmate.goal.domain.GoalStatus;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.goal.domain.TeamMateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

    @Override
    public Optional<TeamMate> findTeamMate(long teamMateId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(teamMate)
                        .join(teamMate.goal, goal).fetchJoin()
                        .where(teamMate.id.eq(teamMateId))
                        .fetchOne());
    }

    @Override
    public Optional<TeamMate> findTeamMate(long goalId, long userId) {
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
        List<TeamMate> yesterDayTMs = queryFactory
                .select(teamMate)
                .from(teamMate)
                .join(teamMate.goal, goal)
                .where(
                        goal.weekDays.weekDays
                                .divide(WeekDayConverter.localDateToValue(LocalDate.now().minusDays(1)))
                                .floor().mod(10)
                                .eq(1),
                        goal.goalStatus.eq(GoalStatus.ONGOING),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING))
                .fetch();

        List<Long> checkedTeamMateIds = queryFactory
                .select(post.teamMate.id)
                .from(post)
                .where(
                        post.teamMate.in(yesterDayTMs),
                        post.uploadedDate.eq(LocalDate.now()),
                        post.isChecked.isTrue())
                .fetch();
        yesterDayTMs.removeIf(tm -> checkedTeamMateIds.contains(tm.getId()));

        queryFactory.update(teamMate)
                .where(teamMate.in(yesterDayTMs))
                .set(teamMate.progressInfo.hookyDays, teamMate.progressInfo.hookyDays.add(1))
                .execute();

        return yesterDayTMs;
    }

    @Override
    public List<TeamMate> eliminateOveredTMs(List<TeamMate> hookyTMs) {
        // TODO: 2022/08/25 TM의 hookyCount를 초기에 max로 해놓고 점점 줄이는걸로 바꾸기
        List<TeamMate> eliminators = hookyTMs.stream()
                .filter(tm -> tm.getHookyDays() >= tm.getGoal().getHookyDayLimit())
                .collect(Collectors.toList());

        queryFactory.update(teamMate)
                .where(teamMate.in(eliminators))
                .set(teamMate.teamMateStatus, TeamMateStatus.OUT)
                .execute();

        return eliminators;
    }

    @Override
    public List<Long> findTeamMateUserIds(Long goalId) {
        return queryFactory
                .select(teamMate.userId)
                .from(teamMate)
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING))
                .fetch();
    }

}



































