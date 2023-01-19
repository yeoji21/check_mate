package checkmate.goal.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TeamMateAcceptResult {
    private long goalId;
    private long teamMateId;

    public TeamMateAcceptResult(long goalId, long teamMateId) {
        this.goalId = goalId;
        this.teamMateId = teamMateId;
    }
}
