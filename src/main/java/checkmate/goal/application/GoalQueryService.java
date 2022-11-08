package checkmate.goal.application;

import checkmate.goal.application.dto.response.*;
import checkmate.goal.infrastructure.GoalQueryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalQueryService {
    private final GoalQueryDao goalQueryDao;

    @Transactional(readOnly = true)
    public GoalDetailInfo findGoalDetail(long goalId, long userId) {
        return goalQueryDao.findDetailInfo(goalId, userId).orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public List<GoalSimpleInfo> findOngoingSimpleInfo(long userId) {
        return goalQueryDao.findOngoingSimpleInfo(userId);
    }

    @Transactional(readOnly = true)
    public GoalPeriodInfo findGoalPeriodInfo(long goalId) {
        return goalQueryDao.findGoalPeriodInfo(goalId).orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public List<TodayGoalInfo> findTodayGoalInfo(long userId) {
        return goalQueryDao.findTodayGoalInfo(userId);
    }

    @Transactional(readOnly = true)
    public List<GoalHistoryInfo> findHistoryGoalInfo(long userId) {
        return goalQueryDao.findHistoryGoalInfo(userId);
    }

}
