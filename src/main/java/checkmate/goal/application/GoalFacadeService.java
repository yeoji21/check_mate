package checkmate.goal.application;

import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalViewResult;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.application.dto.response.TeamMateUploadInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: 2023/02/12 이미 존재하는 서비스의 메소드를 호출해서 4번의 쿼리를 호출하고 있음
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
                .getTeamMateId();
    }
}
