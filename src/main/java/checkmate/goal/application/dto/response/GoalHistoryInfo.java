package checkmate.goal.application.dto.response;

import checkmate.goal.domain.*;
import com.querydsl.core.annotations.QueryProjection;
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

    @QueryProjection
    public GoalHistoryInfo(long id,
                           GoalCategory category,
                           String title,
                           LocalDate startDate,
                           LocalDate endDate,
                           LocalTime appointmentTime,
                           int weekDays,
                           int workingDays) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.weekDays = new GoalCheckDays(weekDays).getKorWeekDay();
        Goal goal = Goal.builder()
                .checkDays(this.weekDays)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        TeamMate teamMate = new TeamMate(0);
        try {
            Field workingDaysField = TeamMateProgress.class.getDeclaredField("workingDays");
            workingDaysField.setAccessible(true);
            workingDaysField.setInt(teamMate.getProgress(), workingDays);

            Field goalField = TeamMate.class.getDeclaredField("goal");
            goalField.setAccessible(true);
            goalField.set(teamMate, goal);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        this.achievementRate = teamMate.calcProgressPercent();
    }

    public void setTeamMateNames(List<String> teamMateNames) {
        this.teamMateNames = teamMateNames;
    }

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
        this(id, category, title, startDate, endDate, appointmentTime, weekDays, workingDays);
        this.teamMateNames = teamMateNames;
    }
}
