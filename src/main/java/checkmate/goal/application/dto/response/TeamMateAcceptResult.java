package checkmate.goal.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamMateAcceptResult {
    private long goalId;
    private long teamMateId;

    public TeamMateAcceptResult(long goalId, long teamMateId) {
        this.goalId = goalId;
        this.teamMateId = teamMateId;
    }
}
