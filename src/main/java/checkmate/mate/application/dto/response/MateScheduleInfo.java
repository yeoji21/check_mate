package checkmate.mate.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class MateScheduleInfo {
    private LocalDate startDate;
    private LocalDate endDate;
    private String goalSchedule;
    private String mateSchedule;

    @QueryProjection
    @Builder
    public MateScheduleInfo(LocalDate startDate,
                            LocalDate endDate,
                            int weekDays,
                            List<LocalDate> uploadedDates) {
        Goal goal = createGoal(startDate, endDate, weekDays);
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalSchedule = goal.getSchedule();
        this.mateSchedule = goal.getSchedule(uploadedDates);
        if (goalSchedule.length() != mateSchedule.length())
            throw new IllegalArgumentException();
    }

    private Goal createGoal(LocalDate startDate, LocalDate endDate, int weekDays) {
        return Goal.builder()
                .checkDays(new GoalCheckDays(weekDays))
                .period(new GoalPeriod(startDate, endDate))
                .build();
    }
}
