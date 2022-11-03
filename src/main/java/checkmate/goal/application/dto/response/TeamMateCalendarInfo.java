package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TeamMateCalendarInfo {
    private LocalDate startDate;
    private String goalCalendar;
    private String teamMateCalendar;

    @Builder
    public TeamMateCalendarInfo(LocalDate startDate,
                                String goalCalendar,
                                String teamMateCalendar) {
        this.startDate = startDate;
        this.goalCalendar = goalCalendar;
        this.teamMateCalendar = teamMateCalendar;
    }

    // TODO: 2022/11/03 필요한 필드만
    @QueryProjection
    public TeamMateCalendarInfo(Goal goal, List<LocalDate> uploadedDates) {
        this.startDate = goal.getStartDate();
        this.goalCalendar = goal.getCalendar();
        this.teamMateCalendar = goal.getStartDate().datesUntil(goal.getEndDate().plusDays(1))
                .map(date -> uploadedDates.contains(date) ? "1" : "0")
                .collect(Collectors.joining());

        if(goalCalendar.length() != teamMateCalendar.length())
            throw new IllegalArgumentException();
    }
}
