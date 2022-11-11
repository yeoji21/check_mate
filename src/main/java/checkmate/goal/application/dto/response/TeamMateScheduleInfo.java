package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.GoalCheckDays;
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

    @QueryProjection @Builder
    public TeamMateScheduleInfo(LocalDate startDate,
                                LocalDate endDate,
                                int weekDays,
                                List<LocalDate> uploadedDates) {
        Goal goal = Goal.builder()
                .checkDays(new GoalCheckDays(weekDays).getKorWeekDay())
                .startDate(startDate)
                .endDate(endDate)
                .build();
        TeamMate teamMate = new TeamMate(0L);
        goal.addTeamMate(teamMate);

        this.startDate = startDate;
        this.endDate = endDate;
        this.goalSchedule = goal.getSchedule();
        this.teamMateSchedule = teamMate.getSchedule(uploadedDates);
        if(goalSchedule.length() != teamMateSchedule.length())
            throw new IllegalArgumentException();
    }
}
