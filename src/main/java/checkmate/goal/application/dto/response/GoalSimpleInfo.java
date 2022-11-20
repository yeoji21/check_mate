package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.GoalCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

public record GoalSimpleInfo(
        long id,
        GoalCategory category,
        String title,
        String weekDays) {
    @Builder @QueryProjection
    public GoalSimpleInfo {
        weekDays = CheckDaysConverter.toDays(Integer.parseInt(weekDays));
    }
}
