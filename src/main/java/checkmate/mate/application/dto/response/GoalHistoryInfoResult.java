package checkmate.mate.application.dto.response;

import checkmate.goal.application.dto.response.GoalHistoryInfo;

import java.util.List;

public record GoalHistoryInfoResult(
        List<GoalHistoryInfo> goals) {
}
