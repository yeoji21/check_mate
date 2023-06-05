package checkmate.goal.infra;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.user.domain.QUser.user;

import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfo;
import checkmate.goal.application.dto.response.QGoalDetailInfo;
import checkmate.goal.application.dto.response.QGoalHistoryInfo;
import checkmate.goal.application.dto.response.QGoalScheduleInfo;
import checkmate.goal.application.dto.response.QOngoingGoalInfo;
import checkmate.goal.application.dto.response.QTodayGoalInfo;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.application.dto.response.QMateUploadInfo;
import checkmate.mate.domain.MateStatus;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.notification.domain.factory.dto.QCompleteGoalNotificationDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class GoalQueryDao {

    private final JPAQueryFactory queryFactory;

    // 오늘 진행할 목표 정보 조회
    public List<TodayGoalInfo> findTodayGoalInfo(long userId) {
        return queryFactory.select(
                new QTodayGoalInfo(goal.id, goal.category, goal.title, goal.checkDays,
                    mate.lastUploadDate))
            .from(mate)
            .join(goal).on(goal.id.eq(mate.goal.id))
            .where(mate.userId.eq(userId),
                mate.status.eq(MateStatus.ONGOING),
                goal.status.eq(GoalStatus.ONGOING),
                goal.checkDays.checkDays.in(CheckDaysConverter.matchingDateValues(LocalDate.now())))
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
        goalDetailInfo.ifPresent(info -> info.setMates(findMatesInfo(goalId)));
        return goalDetailInfo;
    }

    // 목표 진행 일정 관련 정보 조회
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

    // 진행중인 목표들 정보 조회
    public List<OngoingGoalInfo> findOngoingSimpleInfo(long userId) {
        return queryFactory.select(
                new QOngoingGoalInfo(goal.id, goal.category, goal.title, goal.checkDays.checkDays))
            .from(mate)
            .join(mate.goal, goal)
            .where(mate.userId.eq(userId),
                mate.status.eq(MateStatus.ONGOING))
            .fetch();
    }

    private List<MateUploadInfo> findMatesInfo(long goalId) {
        return queryFactory
            .select(new QMateUploadInfo(mate.id, user.id, mate.lastUploadDate, user.nickname))
            .from(mate)
            .join(user).on(mate.userId.eq(user.id))
            .where(mate.goal.id.eq(goalId),
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
        return queryFactory.select(new QGoalHistoryInfo(mate))
            .from(mate)
            .join(mate.goal, goal).fetchJoin()
            .where(mate.userId.eq(userId))
            .fetch();
    }
}
