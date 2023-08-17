package checkmate.goal.application.dto.response;

import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import checkmate.goal.domain.GoalScheduler;
import com.querydsl.core.annotations.QueryProjection;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GoalScheduleInfo implements Serializable {

    private LocalDate startDate;
    private LocalDate endDate;
    private String schedule;

    @Builder
    @QueryProjection
    public GoalScheduleInfo(
        LocalDate startDate,
        LocalDate endDate,
        int weekDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.schedule = getGoalSchedule(startDate, endDate, weekDays);
    }

    private String getGoalSchedule(LocalDate startDate, LocalDate endDate, int weekDays) {
        return GoalScheduler.getTotalSchedule(new GoalPeriod(startDate, endDate),
            GoalCheckDays.ofValue(weekDays));
    }
}
