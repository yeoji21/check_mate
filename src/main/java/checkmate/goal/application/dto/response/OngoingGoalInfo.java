package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.GoalCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.io.Serializable;

public record OngoingGoalInfo(
        long id,
        GoalCategory category,
        String title,
        String weekDays) implements Serializable {
    @Builder
    @QueryProjection
    public OngoingGoalInfo {
        weekDays = CheckDaysConverter.toDays(Integer.parseInt(weekDays));
    }
}
