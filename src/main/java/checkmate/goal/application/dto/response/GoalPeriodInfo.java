package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GoalPeriodInfo {
    private LocalDate startDate;
    private LocalDate endDate;
    private String goalCalendar;

    @Builder
    public GoalPeriodInfo(LocalDate startDate,
                          LocalDate endDate,
                          String goalCalendar) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalCalendar = goalCalendar;
    }

    // TODO: 2022/11/03 필요한 필드만 가져오도록
    @QueryProjection
    public GoalPeriodInfo(Goal goal) {
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.goalCalendar = goal.getCalendar();
    }
}
