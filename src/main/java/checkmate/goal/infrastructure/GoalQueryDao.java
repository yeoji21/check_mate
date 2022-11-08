package checkmate.goal.infrastructure;

import checkmate.common.util.WeekDayConverter;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalStatus;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.user.domain.QUser.user;

@RequiredArgsConstructor
@Repository
public class GoalQueryDao{
    private final JPAQueryFactory queryFactory;

    public List<TodayGoalInfo> findTodayGoalInfo(Long userId) {
        int num = WeekDayConverter.localDateToValue(LocalDate.now());

        return queryFactory
                .select(new QTodayGoalInfo(goal.id, goal.category, goal.title, goal.weekDays,
                            new CaseBuilder()
                                .when(teamMate.lastUploadDay.eq(LocalDate.now()))
                                .then(true)
                                .otherwise(false)))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .on(goal.weekDays.weekDays.divide(num).floor().mod(10).eq(1), goal.goalStatus.eq(GoalStatus.ONGOING))
                .where(teamMate.userId.eq(userId),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING),
                        goal.period.startDate.loe(LocalDate.now()))
                .fetch();
    }

    public List<GoalHistoryInfo> findHistoryGoalInfo(long userId) {
        List<Goal> historyGoals =
                queryFactory.select(goal)
                        .from(teamMate)
                        .innerJoin(teamMate.goal, goal)
                        .where(teamMate.userId.eq(userId),
                                teamMate.teamMateStatus.eq(TeamMateStatus.SUCCESS),
                                goal.goalStatus.eq(GoalStatus.OVER))
                        .fetch();

        Map<Long, List<Tuple>> map =
                queryFactory.select(goal.id, teamMate, user.nickname)
                        .from(teamMate)
                        .innerJoin(user).on(user.id.eq(teamMate.userId))
                        .join(teamMate.goal, goal)
                        .where(goal.in(historyGoals),
                                teamMate.teamMateStatus.eq(TeamMateStatus.SUCCESS))
                        .fetch()
                        .stream()
                        .collect(Collectors.groupingBy(t -> t.get(goal.id)));

        return historyGoals.stream()
                .map(historyGoal ->
                        new GoalHistoryInfo(
                                historyGoal,
                                getSelector(userId, map.get(historyGoal.getId())),
                                getTeamMateNames(map.get(historyGoal.getId()))
                        ))
                .collect(Collectors.toList());
    }

    private List<String> getTeamMateNames(List<Tuple> tuples) {
        return tuples.stream()
                .map(t -> t.get(user.nickname))
                .collect(Collectors.toList());
    }

    private TeamMate getSelector(long userId, List<Tuple> tuples) {
        return tuples
                .stream()
                .filter(t -> t.get(teamMate).getUserId().equals(userId))
                .map(t -> t.get(teamMate))
                .findFirst().orElseThrow();
    }

    public Optional<GoalDetailInfo> findDetailInfo(long goalId, long userId) {
        TeamMate selector = queryFactory
                .select(teamMate)
                .from(teamMate)
                .join(teamMate.goal, goal).fetchJoin()
                .where(teamMate.userId.eq(userId),
                        teamMate.goal.id.eq(goalId))
                .fetchOne();

        return Optional.of(new GoalDetailInfo(selector.getGoal(), selector, findTeamMateInfo(goalId)));
    }

    private List<TeamMateInfo> findTeamMateInfo(long goalId) {
        return queryFactory
                .select(new QTeamMateInfo(teamMate, user.nickname))
                .from(teamMate)
                .join(user).on(teamMate.userId.eq(user.id))
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING))
                .fetch();
    }

    public Optional<GoalPeriodInfo> findGoalPeriodInfo(long goalId) {
        return Optional.ofNullable(
                queryFactory
                        .select(new QGoalPeriodInfo(goal))
                        .from(goal)
                        .where(goal.id.eq(goalId))
                        .fetchOne()
        );
    }

    public List<GoalSimpleInfo> findOngoingSimpleInfo(long userId) {
        return queryFactory.select(new QGoalSimpleInfo(goal.id, goal.category, goal.title, goal.weekDays.weekDays.stringValue()))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .where(teamMate.userId.eq(userId),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING))
                .fetch();
    }
}
