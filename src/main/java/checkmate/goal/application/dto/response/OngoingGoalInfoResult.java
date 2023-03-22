package checkmate.goal.application.dto.response;

import java.io.Serializable;
import java.util.List;

public record OngoingGoalInfoResult(
        List<OngoingGoalInfo> goals) implements Serializable {
}
