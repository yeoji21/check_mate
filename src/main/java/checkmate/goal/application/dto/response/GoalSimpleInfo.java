package checkmate.goal.application.dto.response;

import checkmate.goal.domain.GoalCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

// TODO: 2022/11/11 weekDays 목적
public record GoalSimpleInfo(
        long id,
        GoalCategory category,
        String title,
        String weekDays) {
    @Builder @QueryProjection
    public GoalSimpleInfo {}
}
