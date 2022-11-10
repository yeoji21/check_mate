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

    // 오늘 진행할 목표 정보 조회
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

    /*
    목표 상세 정보 조회
    selector : 조회를 요청한 TeamMate
     */
    public Optional<GoalDetailInfo> findDetailInfo(long goalId, long userId) {
        Optional<GoalDetailInfo> goalDetailInfo = Optional.ofNullable(
                queryFactory
                        .select(new QGoalDetailInfo(goal, teamMate))
                        .from(teamMate)
                        .innerJoin(teamMate.goal, goal)
                        .where(teamMate.userId.eq(userId),
                                teamMate.goal.id.eq(goalId))
                        .fetchOne()
        );
        goalDetailInfo.ifPresent(info -> info.setTeamMates(findTeamMateInfo(goalId)));
        return goalDetailInfo;
    }

    /*
    유저가 이전에 완수한 목표들의 정보
     */
    public List<GoalHistoryInfo> findHistoryGoalInfo(long userId) {
        // 완수한 목표 조회
        List<Goal> historyGoals =
                queryFactory.select(goal)
                        .from(teamMate)
                        .innerJoin(teamMate.goal, goal)
                        .where(teamMate.userId.eq(userId),
                                teamMate.teamMateStatus.eq(TeamMateStatus.SUCCESS),
                                goal.goalStatus.eq(GoalStatus.OVER))
                        .fetch();

        // 조회를 요청한 팀원과 다른 팀원들의 닉네임을 가져오기 위한 쿼리
        Map<Long, List<Tuple>> map =
                queryFactory.select(goal.id, teamMate.userId, teamMate.teamMateProgress.workingDays, user.nickname)
                        .from(teamMate)
                        .innerJoin(user).on(user.id.eq(teamMate.userId))
                        .join(teamMate.goal, goal)
                        .where(goal.in(historyGoals))
                        .fetch()
                        .stream()
                        .collect(Collectors.groupingBy(t -> t.get(goal.id)));

        return historyGoals
                .stream()
                .map(hGoal -> GoalHistoryInfo.builder()
                            .id(hGoal.getId())
                            .title(hGoal.getTitle())
                            .category(hGoal.getCategory())
                            .startDate(hGoal.getStartDate())
                            .endDate(hGoal.getEndDate())
                            .weekDays(hGoal.getWeekDays().intValue())
                            .appointmentTime(hGoal.getAppointmentTime())
                            .workingDays(getTeamMateWorkingDays(userId, map.get(hGoal.getId())))
                            .teamMateNames(getTeamMateNames(map.get(hGoal.getId())))
                            .build())
                .toList();
    }


    // 목표 진행 일정 관련 정보 조회
    public Optional<GoalScheduleInfo> findGoalScheduleInfo(long goalId) {
        return Optional.ofNullable(
                queryFactory
                        .select(new QGoalScheduleInfo(goal.period.startDate, goal.period.endDate, goal.weekDays.weekDays))
                        .from(goal)
                        .where(goal.id.eq(goalId))
                        .fetchOne()
        );
    }

    // 진행중인 목표들 정보 조회
    public List<GoalSimpleInfo> findOngoingSimpleInfo(long userId) {
        return queryFactory.select(new QGoalSimpleInfo(goal.id, goal.category, goal.title, goal.weekDays.weekDays.stringValue()))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .where(teamMate.userId.eq(userId),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING))
                .fetch();
    }

    private int getTeamMateWorkingDays(long userId, List<Tuple> tuples) {
        return tuples.stream()
                .filter(t -> t.get(teamMate.userId) == userId)
                .findAny()
                .map(t -> t.get(teamMate.teamMateProgress.workingDays))
                .orElseThrow();
    }

    private List<String> getTeamMateNames(List<Tuple> tuples) {
        return tuples.stream()
                .map(t -> t.get(user.nickname))
                .toList();
    }

    private TeamMate getSelector(long userId, List<Tuple> tuples) {
        return tuples
                .stream()
                .filter(t -> t.get(teamMate).getUserId().equals(userId))
                .map(t -> t.get(teamMate))
                .findFirst()
                .orElseThrow();
    }

    private List<TeamMateUploadInfo> findTeamMateInfo(long goalId) {
        return queryFactory
                .select(new QTeamMateUploadInfo(teamMate.id, user.id, teamMate.lastUploadDay, user.nickname))
                .from(teamMate)
                .join(user).on(teamMate.userId.eq(user.id))
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.teamMateStatus.eq(TeamMateStatus.ONGOING))
                .fetch();
    }
}
