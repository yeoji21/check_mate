package checkmate.goal.application;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfoResult;
import checkmate.goal.application.dto.response.TodayGoalInfoResult;
import checkmate.goal.infra.GoalQueryDao;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
import checkmate.mate.infra.MateQueryDao;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
    private final MateQueryDao mateQueryDao;

    @Transactional(readOnly = true)
    public GoalDetailInfo findGoalDetail(long goalId) {
        return goalQueryDao.findDetailInfo(goalId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    @Cacheable(
        value = CacheKey.ONGOING_GOALS,
        key = "{#userId, T(java.time.LocalDate).now().format(@dateFormatter)}"
    )
    @Transactional(readOnly = true)
    public OngoingGoalInfoResult findOngoingGoalInfo(long userId) {
        return new OngoingGoalInfoResult(goalQueryDao.findOngoingSimpleInfo(userId));
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

    @Cacheable(value = CacheKey.HISTORY_GOALS, key = "{#userId}")
    @Transactional(readOnly = true)
    public GoalHistoryInfoResult findGoalHistoryResult(long userId) {
        List<GoalHistoryInfo> historyInfo = goalQueryDao.findGoalHistoryInfo(userId);
        Map<Long, List<String>> mateNicknames = mateQueryDao.findMateNicknames(
            mapToGoalId(historyInfo));
        historyInfo.forEach(setNicknamesToHistoryInfo(mateNicknames));
        return new GoalHistoryInfoResult(historyInfo);
    }

    private List<Long> mapToGoalId(List<GoalHistoryInfo> historyInfo) {
        return historyInfo.stream().map(GoalHistoryInfo::getGoalId).toList();
    }

    private Consumer<GoalHistoryInfo> setNicknamesToHistoryInfo(
        Map<Long, List<String>> mateNicknames) {
        return historyInfo -> historyInfo.setMateNicknames(
            mateNicknames.get(historyInfo.getGoalId()));
    }
}
