package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.TeamMate;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class GoalHistoryInfo {
    private long id;
    private GoalCategory category;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private String weekDays;
    private double achievementRate;
    private List<String> teamMateNames;

    @Builder
    public GoalHistoryInfo(long id,
                           GoalCategory category,
                           String title,
                           LocalDate startDate,
                           LocalDate endDate,
                           LocalTime appointmentTime,
                           String weekDays,
                           double achievementRate,
                           List<String> teamMateNames) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.weekDays = weekDays;
        this.achievementRate = achievementRate;
        this.teamMateNames = teamMateNames;
    }

    @QueryProjection @Builder
    public GoalHistoryInfo(Goal goal, TeamMate selector, List<String> teamMateNames) {
        this.id = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.weekDays = goal.getWeekDays().getKorWeekDay();
        this.achievementRate = selector.calcProgressPercent();
        this.teamMateNames = teamMateNames;
    }
}
