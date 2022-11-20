package checkmate.goal.infrastructure;

import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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
    private final EntityManager entityManager;

    // 오늘 진행할 목표 정보 조회
    public List<TodayGoalInfo> findTodayGoalInfo(Long userId) {
        List<Object[]> resultList = entityManager.createNativeQuery(
                        "select g.goal_id, g.category, g.title, g.check_days, " +
                                " case when tm.last_upload_day = :today then true else false end" +
                                " from team_mate as tm" +
                                " join goal as g on g.goal_id = tm.goal_id" +
                                " where tm.user_id = :userId" +
                                " and BITAND(g.check_days," + (1 << CheckDaysConverter.valueOf(LocalDate.now().getDayOfWeek().toString()).getValue()) +") != 0" +
                                " and tm.status = 'ONGOING' and g.start_date <= :today")
                .setParameter("today", LocalDate.now())
                .setParameter("userId", userId)
                .getResultList();
        return resultList.stream()
                .map(arr -> new TodayGoalInfo(Long.parseLong(String.valueOf(arr[0])), GoalCategory.valueOf(String.valueOf(arr[1])),
                        (String) arr[2], new GoalCheckDays(Integer.parseInt(String.valueOf(arr[3]))), (Boolean) arr[4]))
                .toList();
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
