package checkmate.mate.application;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.goal.application.dto.response.GoalDetailResult;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfoResult;
import checkmate.goal.infrastructure.TeamMateQueryDao;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static checkmate.exception.code.ErrorCode.TEAM_MATE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MateQueryService {
    private final TeamMateQueryDao teamMateQueryDao;
    private final MateRepository mateRepository;

    @Transactional(readOnly = true)
    public MateScheduleInfo getCalenderInfo(long teamMateId) {
        return teamMateQueryDao.getTeamMateCalendar(teamMateId)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public double getProgressPercent(long teamMateId) {
        Mate mate = mateRepository.findTeamMateWithGoal(teamMateId)
                .orElseThrow(() -> new NotFoundException(TEAM_MATE_NOT_FOUND, teamMateId));
        return mate.calcProgressPercent();
    }

    @Transactional(readOnly = true)
    public GoalDetailResult findGoalDetailResult(long goalId, long userId) {
        Mate mate = mateRepository.findTeamMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(TEAM_MATE_NOT_FOUND));
        return new GoalDetailResult(mate,
                teamMateQueryDao.findUploadedDates(mate.getId()),
                teamMateQueryDao.findTeamMateInfo(goalId));
    }

    @Cacheable(value = CacheKey.HISTORY_GOALS, key = "{#userId}")
    @Transactional(readOnly = true)
    public GoalHistoryInfoResult findHistoryGoalInfo(long userId) {
        List<Mate> successMates = teamMateQueryDao.findSuccessTeamMates(userId);
        return new GoalHistoryInfoResult(mapToGoalHistoryInfo(successMates));
    }

    private List<GoalHistoryInfo> mapToGoalHistoryInfo(List<Mate> successMates) {
        Map<Long, List<String>> teamMateNicknames = teamMateQueryDao.findTeamMateNicknames(getGoalIds(successMates));
        return successMates.stream()
                .map(teamMate -> new GoalHistoryInfo(teamMate, teamMateNicknames.get(teamMate.getGoal().getId())))
                .toList();
    }

    private List<Long> getGoalIds(List<Mate> successMates) {
        return successMates.stream()
                .map(teamMate -> teamMate.getGoal().getId())
                .toList();
    }
}
