package checkmate.goal.application.dto.response;

import java.io.Serializable;
import java.util.List;

public record TodayGoalInfoResult(
        List<TodayGoalInfo> goals) implements Serializable {
}
