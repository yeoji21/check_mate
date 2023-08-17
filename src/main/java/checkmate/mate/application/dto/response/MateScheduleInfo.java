package checkmate.mate.application.dto.response;

import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import checkmate.goal.domain.GoalScheduler;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MateScheduleInfo {

    private LocalDate startDate;
    private LocalDate endDate;
    private String goalSchedule;
    private String mateSchedule;

    @QueryProjection
    @Builder
    public MateScheduleInfo(
        LocalDate startDate,
        LocalDate endDate,
        int weekDays,
        List<LocalDate> uploadedDates) {
        this.startDate = startDate;
        this.endDate = endDate;
        GoalCheckDays checkDays = GoalCheckDays.ofValue(weekDays);
        GoalPeriod period = new GoalPeriod(startDate, endDate);
        this.goalSchedule = GoalScheduler.getTotalSchedule(period, checkDays);
        this.mateSchedule = GoalScheduler.getCheckedSchedule(period, checkDays,
            uploadedDates);
    }
}
