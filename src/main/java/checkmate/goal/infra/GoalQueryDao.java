package checkmate.goal.infra;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfo;
import checkmate.goal.application.dto.response.QGoalDetailInfo;
import checkmate.goal.application.dto.response.QGoalDetailInfo_MateInfo;
import checkmate.goal.application.dto.response.QGoalHistoryInfo;
import checkmate.goal.application.dto.response.QGoalScheduleInfo;
import checkmate.goal.application.dto.response.QOngoingGoalInfo;
import checkmate.goal.application.dto.response.QTodayGoalInfo;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.notification.domain.factory.dto.QCompleteGoalNotificationDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class GoalQueryDao {

    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;

    public void updateStatusToOver(List<Long> goalIds) {
        jdbcTemplate.batchUpdate("UPDATE goal SET status = ? WHERE id = ?",
            goalIds,
            goalIds.size(),
            (ps, id) -> {
                ps.setString(1, GoalStatus.OVER.name());
                ps.setLong(2, id);
            });
    }

    public void updateTodayStartGoalsToOngoing() {
        jdbcTemplate.batchUpdate(
            "UPDATE goal SET status = 'ONGOING' WHERE start_date = ? AND status = 'WAITING'",
            Collections.singletonList(new Object[]{LocalDate.now()})
        );
    }

    public List<TodayGoalInfo> findTodayGoalInfo(long userId) {
        return queryFactory.select(
                new QTodayGoalInfo(goal.id, goal.category, goal.title, goal.checkDays,
                    mate.lastUploadDate))
            .from(mate)
            .join(goal).on(goal.id.eq(mate.goal.id))
            .where(
                mate.userId.eq(userId),
                mate.status.eq(MateStatus.ONGOING),
                goal.status.eq(GoalStatus.ONGOING),
                goal.checkDays.checkDays.in(
                    CheckDaysConverter.getAllPossibleValues(LocalDate.now().getDayOfWeek())))
            .fetch();
    }

    public Optional<GoalDetailInfo> findDetailInfo(long goalId) {
        Optional<GoalDetailInfo> goalDetailInfo = Optional.ofNullable(
            queryFactory
                .select(new QGoalDetailInfo(goal))
                .from(goal)
                .where(goal.id.eq(goalId),
                    goal.status.eq(GoalStatus.ONGOING))
                .fetchOne()
        );
        goalDetailInfo.ifPresent(info -> info.setMates(findMateInfo(goalId)));
        return goalDetailInfo;
    }

    public Optional<GoalScheduleInfo> findGoalScheduleInfo(long goalId) {
        return Optional.ofNullable(
            queryFactory
                .select(new QGoalScheduleInfo(goal.period.startDate, goal.period.endDate,
                    goal.checkDays.checkDays))
                .from(goal)
                .where(goal.id.eq(goalId))
                .fetchOne()
        );
    }

    public List<OngoingGoalInfo> findOngoingGoalInfo(long userId) {
        return queryFactory.select(
                new QOngoingGoalInfo(goal.id, goal.category, goal.title, goal.checkDays.checkDays))
            .from(mate)
            .join(mate.goal, goal)
            .where(mate.userId.eq(userId),
                mate.status.eq(MateStatus.ONGOING))
            .fetch();
    }

    public List<Long> findYesterdayOveredGoals() {
        return queryFactory
            .select(goal.id)
            .from(goal)
            .where(goal.period.endDate.eq(LocalDate.now().minusDays(1)),
                goal.status.eq(GoalStatus.ONGOING))
            .fetch();
    }

    public List<CompleteGoalNotificationDto> findCompleteNotificationDto(List<Long> goalIds) {
        return queryFactory.select(
                new QCompleteGoalNotificationDto(mate.userId, goal.id, goal.title))
            .from(mate)
            .join(mate.goal, goal)
            .where(mate.goal.id.in(goalIds),
                mate.status.eq(MateStatus.ONGOING))
            .fetch();
    }

    public List<GoalHistoryInfo> findGoalHistoryInfo(long userId) {
        List<GoalHistoryInfo> goalHistoryInfo = queryFactory.select(new QGoalHistoryInfo(mate))
            .from(mate)
            .join(mate.goal, goal).fetchJoin()
            .where(mate.userId.eq(userId))
            .fetch();
        setNicknamesToEachGoalInfo(goalHistoryInfo);
        return goalHistoryInfo;
    }

    private List<GoalDetailInfo.MateInfo> findMateInfo(long goalId) {
        return queryFactory
            .select(
                new QGoalDetailInfo_MateInfo(mate.id, user.id, mate.lastUploadDate, user.nickname))
            .from(mate)
            .join(user).on(mate.userId.eq(user.id))
            .where(mate.goal.id.eq(goalId),
                mate.status.eq(MateStatus.ONGOING))
            .fetch();
    }

    private List<Long> mapToGoalId(List<GoalHistoryInfo> historyInfo) {
        return historyInfo.stream().map(GoalHistoryInfo::getGoalId).toList();
    }

    private Map<Long, List<String>> findMateNicknames(List<Long> goalIds) {
        return queryFactory
            .from(goal)
            .leftJoin(mate).on(mate.goal.eq(goal))
            .join(user).on(mate.userId.eq(user.id))
            .where(goal.id.in(goalIds))
            .transform(groupBy(goal.id).as(list(user.nickname)));
    }

    private void setNicknamesToEachGoalInfo(List<GoalHistoryInfo> goalHistoryInfo) {
        Map<Long, List<String>> mateNicknames = findMateNicknames(mapToGoalId(goalHistoryInfo));
        goalHistoryInfo.forEach(historyInfo -> historyInfo.setMateNicknames(
            mateNicknames.get(historyInfo.getGoalId())));
    }

    public List<Long> findOngoingUserIds(List<Long> goalIds) {
        return queryFactory.select(mate.userId)
            .from(mate)
            .where(mate.goal.id.in(goalIds), mate.status.eq(MateStatus.ONGOING))
            .fetch();
    }
}
