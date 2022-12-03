package checkmate.goal.application;

import checkmate.exception.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.infrastructure.GoalQueryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public GoalSimpleInfoResult findOngoingSimpleInfo(long userId) {
        return new GoalSimpleInfoResult(goalQueryDao.findOngoingSimpleInfo(userId));
    }

    @Transactional(readOnly = true)
    public GoalScheduleInfo findGoalPeriodInfo(long goalId) {
        return goalQueryDao.findGoalScheduleInfo(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    @Transactional(readOnly = true)
    public TodayGoalInfoResult findTodayGoalInfo(long userId) {
        return new TodayGoalInfoResult(goalQueryDao.findTodayGoalInfo(userId));
    }

    @Transactional(readOnly = true)
    public GoalHistoryInfoResult findHistoryGoalInfo(long userId) {
        return new GoalHistoryInfoResult(goalQueryDao.findHistoryGoalInfo(userId));
    }

}
