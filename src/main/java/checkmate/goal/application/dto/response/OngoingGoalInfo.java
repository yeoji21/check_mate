package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import com.querydsl.core.annotations.QueryProjection;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OngoingGoalInfo implements Serializable {

    private long goalId;
    private GoalCategory category;
    private String title;
    private String weekDays;

    @Builder
    @QueryProjection
    public OngoingGoalInfo(
        long goalId,
        GoalCategory category,
        String title,
        int weekDays) {
        this.goalId = goalId;
        this.category = category;
        this.title = title;
        this.weekDays = CheckDaysConverter.toKorean(GoalCheckDays.ofValue(weekDays));
    }
}
