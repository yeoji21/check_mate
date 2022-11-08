package checkmate.goal.application;

import checkmate.exception.TeamMateNotFoundException;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalViewResult;
import checkmate.goal.application.dto.response.TeamMateCalendarInfo;
import checkmate.goal.application.dto.response.TeamMateInfo;
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
        TeamMateCalendarInfo calenderInfo = teamMateQueryService.getCalenderInfo(teamMateId);

        return new GoalViewResult(goalDetailInfo, calenderInfo, progress);
    }

    private long getTeamMateId(long userId, List<TeamMateInfo> teamMates) {
        return teamMates.stream()
                .filter(tm -> tm.getUserId() == userId)
                .findAny()
                .orElseThrow(TeamMateNotFoundException::new)
                .getId();
    }
}
