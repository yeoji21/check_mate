package checkmate.goal.application;

import checkmate.exception.TeamMateNotFoundException;
import checkmate.goal.application.dto.GoalQueryMapper;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalDetailViewResult;
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
    private final GoalQueryMapper mapper;

    public GoalDetailViewResult goalDetailView(long goalId, long userId) {
        GoalDetailInfo goalDetail = goalQueryService.findGoalDetail(goalId, userId);
        long teamMateId = getTeamMateId(userId, goalDetail.getTeamMates());
        double progress = teamMateQueryService.getProgressPercent(teamMateId);
        TeamMateCalendarInfo calenderInfo = teamMateQueryService.getCalenderInfo(teamMateId);
        return mapper.toGoalDetailViewResult(goalDetail, calenderInfo, progress);
    }

    private long getTeamMateId(long userId, List<TeamMateInfo> teamMates) {
        return teamMates.stream()
                .filter(tm -> tm.getUserId() == userId)
                .findAny()
                .orElseThrow(TeamMateNotFoundException::new)
                .getId();
    }
}
