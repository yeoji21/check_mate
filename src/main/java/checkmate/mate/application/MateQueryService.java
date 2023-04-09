package checkmate.mate.application;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.SpecifiedGoalDetailInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static checkmate.exception.code.ErrorCode.MATE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MateQueryService {
    private final MateQueryDao mateQueryDao;
    private final MateRepository mateRepository;

    @Transactional(readOnly = true)
    public MateScheduleInfo findCalenderInfo(long teamMateId) {
        return mateQueryDao.findMateCalendar(teamMateId)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public SpecifiedGoalDetailInfo findSpecifiedGoalDetailInfo(long goalId, long userId) {
        Mate mate = mateRepository.findMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(MATE_NOT_FOUND));
        return new SpecifiedGoalDetailInfo(mate,
                mateQueryDao.findUploadedDates(mate.getId()),
                mateQueryDao.findUploadInfo(goalId));
    }

    @Cacheable(value = CacheKey.HISTORY_GOALS, key = "{#userId}")
    @Transactional(readOnly = true)
    public GoalHistoryInfoResult findGoalHistoryResult(long userId) {
        List<Mate> successMates = mateQueryDao.findSuccessMates(userId);
        return new GoalHistoryInfoResult(mapToGoalHistoryInfo(successMates));
    }

    private List<GoalHistoryInfo> mapToGoalHistoryInfo(List<Mate> successMates) {
        Map<Long, List<String>> mateNicknames = mateQueryDao.findMateNicknames(getGoalIds(successMates));
        return successMates.stream()
                .map(mate -> new GoalHistoryInfo(mate, mateNicknames.get(mate.getGoal().getId())))
                .toList();
    }

    private List<Long> getGoalIds(List<Mate> successMates) {
        return successMates.stream()
                .map(mate -> mate.getGoal().getId())
                .toList();
    }
}
