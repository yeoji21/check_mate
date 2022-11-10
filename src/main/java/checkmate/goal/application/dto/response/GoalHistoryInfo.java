package checkmate.goal.application.dto.response;

import checkmate.goal.domain.*;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Field;
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
                           int weekDays,
                           int workingDays,
                           List<String> teamMateNames) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.weekDays = new WeekDays(weekDays).getKorWeekDay();
        Goal goal = Goal.builder()
                .weekDays(this.weekDays)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        TeamMate teamMate = new TeamMate(0);
        goal.addTeamMate(teamMate);
        try {
            Field field = TeamMateProgress.class.getDeclaredField("workingDays");
            field.setAccessible(true);
            field.setInt(teamMate.getTeamMateProgress(), workingDays);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        this.achievementRate = teamMate.calcProgressPercent();
        this.teamMateNames = teamMateNames;
    }
}
