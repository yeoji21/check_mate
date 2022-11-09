package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.WeekDays;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GoalScheduleInfo {
    private LocalDate startDate;
    private LocalDate endDate;
    private String schedule;

    @Builder @QueryProjection
    public GoalScheduleInfo(LocalDate startDate,
                            LocalDate endDate,
                            int weekDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.schedule = Goal.builder()
                .weekDays(new WeekDays(weekDays).getKorWeekDay())
                .startDate(startDate)
                .endDate(endDate)
                .build().getSchedule();
    }
}
