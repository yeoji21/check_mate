package checkmate.goal.application;

import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.NotFoundException;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalViewResult;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.application.dto.response.TeamMateUploadInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GoalFacadeService {
    private final GoalQueryService goalQueryService;
    private final TeamMateQueryService teamMateQueryService;

    public GoalViewResult goalDetailView(long goalId, long userId) {
        GoalDetailInfo goalDetailInfo = goalQueryService.findGoalDetail(goalId, userId);
        long teamMateId = getTeamMateId(userId, goalDetailInfo.getTeamMates());
        double progress = teamMateQueryService.getProgressPercent(teamMateId);
        TeamMateScheduleInfo calenderInfo = teamMateQueryService.getCalenderInfo(teamMateId);

        return new GoalViewResult(goalDetailInfo, calenderInfo, progress);
    }

    private long getTeamMateId(long userId, List<TeamMateUploadInfo> teamMates) {
        return teamMates.stream()
                .filter(tm -> tm.getUserId() == userId)
                .findAny()
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MATE_NOT_FOUND))
                .getId();
    }
}
