package checkmate.goal.infra;

import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalStatus;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.application.dto.response.QMateUploadInfo;
import checkmate.mate.domain.MateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.user.domain.QUser.user;

@RequiredArgsConstructor
@Repository
public class GoalQueryDao {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    // 오늘 진행할 목표 정보 조회
    // TODO: 2023/04/08 인덱스 고려
    public List<TodayGoalInfo> findTodayGoalInfo(Long userId) {
        List<Object[]> resultList = entityManager.createNativeQuery(
                        "select g.id, g.category, g.title, g.check_days, m.last_upload_date" +
                                " from mate as m" +
                                " join goal as g on g.id = m.goal_id" +
                                " where m.user_id = :userId" +
                                " and g.check_days in :values" +
                                " and m.status = 'ONGOING' and g.status = 'ONGOING'")
                .setParameter("userId", userId)
                .setParameter("values", CheckDaysConverter.matchingDateValues(LocalDate.now()))
                .getResultList();

        return resultList.stream()
                .map(arr -> new TodayGoalInfo(Long.parseLong(String.valueOf(arr[0])), GoalCategory.valueOf(String.valueOf(arr[1])),
                        (String) arr[2], new GoalCheckDays(Integer.parseInt(String.valueOf(arr[3]))), (LocalDate) arr[4]))
                .toList();
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
        goalDetailInfo.ifPresent(info -> info.setMates(findTeamMateInfo(goalId)));
        return goalDetailInfo;
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
    public List<OngoingGoalInfo> findOngoingSimpleInfo(long userId) {
        return queryFactory.select(new QOngoingGoalInfo(goal.id, goal.category, goal.title, goal.checkDays.checkDays.stringValue()))
                .from(mate)
                .join(mate.goal, goal)
                .where(mate.userId.eq(userId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetch();
    }

    private List<MateUploadInfo> findTeamMateInfo(long goalId) {
        return queryFactory
                .select(new QMateUploadInfo(mate.id, user.id, mate.lastUploadDate, user.nickname))
                .from(mate)
                .join(user).on(mate.userId.eq(user.id))
                .where(mate.goal.id.eq(goalId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetch();
    }
}
