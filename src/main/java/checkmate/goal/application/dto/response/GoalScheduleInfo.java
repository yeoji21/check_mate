package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
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
    public GoalScheduleInfo(LocalDate startDate,
        LocalDate endDate,
        int weekDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.schedule = getSchedule(startDate, endDate, weekDays);
    }

    private String getSchedule(LocalDate startDate, LocalDate endDate, int weekDays) {
        return Goal.builder()
            .checkDays(GoalCheckDays.ofValue(weekDays))
            .period(new GoalPeriod(startDate, endDate))
            .build()
            .getSchedule();
    }
}
