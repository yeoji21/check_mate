package checkmate.goal.application;

import checkmate.exception.NotFoundException;
import checkmate.goal.application.dto.response.GoalDetailResult;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.application.dto.response.GoalHistoryInfoResult;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.goal.infrastructure.TeamMateQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static checkmate.exception.code.ErrorCode.TEAM_MATE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class TeamMateQueryService {
    private final TeamMateQueryDao teamMateQueryDao;
    private final TeamMateRepository teamMateRepository;

    @Transactional(readOnly = true)
    public TeamMateScheduleInfo getCalenderInfo(long teamMateId) {
        return teamMateQueryDao.getTeamMateCalendar(teamMateId)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public double getProgressPercent(long teamMateId) {
        TeamMate teamMate = teamMateRepository.findTeamMateWithGoal(teamMateId)
                .orElseThrow(() -> new NotFoundException(TEAM_MATE_NOT_FOUND, teamMateId));
        return teamMate.calcProgressPercent();
    }

    @Transactional(readOnly = true)
    public GoalDetailResult findGoalDetailResult(long goalId, long userId) {
        TeamMate teamMate = teamMateRepository.findTeamMateWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(TEAM_MATE_NOT_FOUND));
        return new GoalDetailResult(teamMate,
                teamMateQueryDao.findUploadedDates(teamMate.getId()),
                teamMateQueryDao.findTeamMateInfo(goalId));
    }

    @Transactional(readOnly = true)
    public GoalHistoryInfoResult findHistoryGoalInfo(long userId) {
        List<TeamMate> successTeamMates = teamMateQueryDao.findSuccessTeamMates(userId);
        return new GoalHistoryInfoResult(mapToGoalHistoryInfo(successTeamMates));
    }

    private List<GoalHistoryInfo> mapToGoalHistoryInfo(List<TeamMate> successTeamMates) {
        Map<Long, List<String>> teamMateNicknames = teamMateQueryDao.findTeamMateNicknames(getGoalIds(successTeamMates));
        return successTeamMates.stream()
                .map(teamMate -> new GoalHistoryInfo(teamMate, teamMateNicknames.get(teamMate.getGoal().getId())))
                .toList();
    }

    private List<Long> getGoalIds(List<TeamMate> successTeamMates) {
        return successTeamMates.stream()
                .map(teamMate -> teamMate.getGoal().getId())
                .toList();
    }
}
