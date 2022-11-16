package checkmate.goal.infrastructure;

import checkmate.common.util.WeekDayConverter;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.GoalStatus;
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
                .select(new QTodayGoalInfo(goal.id, goal.category, goal.title, goal.checkDays,
                            new CaseBuilder()
                                .when(teamMate.lastUploadDay.eq(LocalDate.now()))
                                .then(true)
                                .otherwise(false)))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .on(goal.checkDays.checkDays.divide(num).floor().mod(10).eq(1), goal.status.eq(GoalStatus.ONGOING))
                .where(teamMate.userId.eq(userId),
                        teamMate.status.eq(TeamMateStatus.ONGOING),
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

    //유저가 이전에 완수한 목표들의 정보
    public List<GoalHistoryInfo> findHistoryGoalInfo(long userId) {
        // 완수한 목표 조회
        List<GoalHistoryInfo> list = queryFactory.select(
                        new QGoalHistoryInfo(goal.id, goal.category, goal.title, goal.period.startDate,
                                goal.period.endDate, goal.appointmentTime, goal.checkDays.checkDays,
                                teamMate.progress.workingDays))
                .from(teamMate)
                .innerJoin(teamMate.goal, goal)
                .where(teamMate.userId.eq(userId),
                        teamMate.status.eq(TeamMateStatus.SUCCESS),
                        goal.status.eq(GoalStatus.OVER))
                .fetch();
        List<Long> goalIds = list.stream().map(GoalHistoryInfo::getId).toList();

        // 팀원들의 닉네임을 조회 후 goalID를 기준으로 grouping
        Map<Long, List<Tuple>> map =
                queryFactory.select(goal.id, user.nickname)
                        .from(teamMate)
                        .innerJoin(user).on(user.id.eq(teamMate.userId))
                        .join(teamMate.goal, goal)
                        .where(goal.id.in(goalIds))
                        .fetch()
                        .stream()
                        .collect(Collectors.groupingBy(t -> t.get(goal.id)));
        list.forEach(info -> {
            List<String> nicknames = map.get(info.getId()).stream().map(t -> t.get(user.nickname)).toList();
            info.setTeamMateNames(nicknames);
        });
        return list;
    }


    // 목표 진행 일정 관련 정보 조회
    public Optional<GoalScheduleInfo> findGoalScheduleInfo(long goalId) {
        return Optional.ofNullable(
                queryFactory
                        .select(new QGoalScheduleInfo(goal.period.startDate, goal.period.endDate, goal.checkDays.checkDays))
                        .from(goal)
                        .where(goal.id.eq(goalId))
                        .fetchOne()
        );
    }

    // 진행중인 목표들 정보 조회
    public List<GoalSimpleInfo> findOngoingSimpleInfo(long userId) {
        return queryFactory.select(new QGoalSimpleInfo(goal.id, goal.category, goal.title, goal.checkDays.checkDays.stringValue()))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .where(teamMate.userId.eq(userId),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetch();
    }

    private List<TeamMateUploadInfo> findTeamMateInfo(long goalId) {
        return queryFactory
                .select(new QTeamMateUploadInfo(teamMate.id, user.id, teamMate.lastUploadDay, user.nickname))
                .from(teamMate)
                .join(user).on(teamMate.userId.eq(user.id))
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetch();
    }
}
