package checkmate.goal.application;

import checkmate.goal.application.dto.GoalQueryMapper;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infrastructure.GoalQueryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalQueryService {
    private final GoalQueryDao goalQueryDao;
    private final GoalRepository goalRepository;
    private final GoalQueryMapper mapper;

    @Transactional(readOnly = true)
    public GoalDetailInfo findGoalDetail(long goalId, long userId) {
        return goalQueryDao.findDetailInfo(goalId, userId).orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public List<GoalSimpleInfo> findOngoingSimpleInfo(long userId) {
        return goalRepository.findOngoingGoalList(userId)
                .stream()
                .map(mapper::toGoalSimpleInfo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GoalPeriodInfo findGoalPeriodInfo(long goalId) {
        return goalQueryDao.findGoalPeriodInfo(goalId).orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public List<TodayGoalInfo> findTodayGoalInfo(long userId) {
        return goalQueryDao.findTodayGoalInfoDtoList(userId);
    }

    @Transactional(readOnly = true)
    public List<GoalHistoryInfo> findHistoryGoalInfo(long userId) {
        return goalQueryDao.findHistoryGoalList(userId);
    }

}
