package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.GoalCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class OngoingGoalInfo implements Serializable {
    private long id;
    private GoalCategory category;
    private String title;
    private String weekDays;

    @Builder
    @QueryProjection
    public OngoingGoalInfo(long id, GoalCategory category, String title, int weekDays) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.weekDays = CheckDaysConverter.toDays(weekDays);
    }
}
