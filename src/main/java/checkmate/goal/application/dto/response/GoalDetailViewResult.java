package checkmate.goal.application.dto.response;

import checkmate.goal.presentation.dto.response.GoalDetailResponse;
import checkmate.goal.presentation.dto.response.TeamMateCalendarResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GoalDetailViewResult {
    private GoalDetailResponse goalDetailResponse;
    private TeamMateCalendarResponse teamMateCalendarResponse;
    private double progress;

    @Builder
    public GoalDetailViewResult(GoalDetailResponse goalDetailResponse,
                                TeamMateCalendarResponse teamMateCalendarResponse,
                                double progress) {
        this.goalDetailResponse = goalDetailResponse;
        this.teamMateCalendarResponse = teamMateCalendarResponse;
        this.progress = progress;
    }
}
