package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class TeamMateScheduleInfo {
    private LocalDate startDate;
    private LocalDate endDate;
    private String goalSchedule;
    private String teamMateSchedule;

    @QueryProjection
    @Builder
    public TeamMateScheduleInfo(LocalDate startDate,
                                LocalDate endDate,
                                int weekDays,
                                List<LocalDate> uploadedDates) {
        Goal goal = createGoal(startDate, endDate, weekDays);
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalSchedule = goal.getSchedule();
        this.teamMateSchedule = goal.getSchedule(uploadedDates);
        if (goalSchedule.length() != teamMateSchedule.length())
            throw new IllegalArgumentException();
    }

    private Goal createGoal(LocalDate startDate, LocalDate endDate, int weekDays) {
        return Goal.builder()
                .checkDays(new GoalCheckDays(weekDays))
                .period(new GoalPeriod(startDate, endDate))
                .build();
    }
}
