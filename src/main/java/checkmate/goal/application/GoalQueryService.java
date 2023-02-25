package checkmate.goal.application;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.GoalSimpleInfoResult;
import checkmate.goal.application.dto.response.TodayGoalInfoResult;
import checkmate.goal.infra.GoalQueryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalQueryService {
    private final GoalQueryDao goalQueryDao;

    @Transactional(readOnly = true)
    public GoalDetailInfo findGoalDetail(long goalId, long userId) {
        return goalQueryDao.findDetailInfo(goalId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    @Cacheable(
            value = CacheKey.ONGOING_GOALS,
            key = "{#userId, T(java.time.LocalDate).now().format(@dateFormatter)}"
    )
    @Transactional(readOnly = true)
    public GoalSimpleInfoResult findOngoingSimpleInfo(long userId) {
        return new GoalSimpleInfoResult(goalQueryDao.findOngoingSimpleInfo(userId));
    }

    @Cacheable(value = CacheKey.GOAL_PERIOD, key = "{#goalId}")
    @Transactional(readOnly = true)
    public GoalScheduleInfo findGoalPeriodInfo(long goalId) {
        return goalQueryDao.findGoalScheduleInfo(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    @Cacheable(
            value = CacheKey.TODAY_GOALS,
            key = "{#userId, T(java.time.LocalDate).now().format(@dateFormatter)}"
    )
    @Transactional(readOnly = true)
    public TodayGoalInfoResult findTodayGoalInfo(long userId) {
        return new TodayGoalInfoResult(goalQueryDao.findTodayGoalInfo(userId));
    }
}
