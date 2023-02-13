package checkmate.goal.application.dto.response;

import checkmate.goal.domain.*;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private String checkDays;
    private double achievementRate;
    private List<String> teamMateNames;

    // TODO: 2023/02/12
    @QueryProjection
    public GoalHistoryInfo(long id,
                           GoalCategory category,
                           String title,
                           LocalDate startDate,
                           LocalDate endDate,
                           LocalTime appointmentTime,
                           int checkDays,
                           int workingDays) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.checkDays = CheckDaysConverter.toDays(checkDays);
        TeamMate teamMate;
        try {
            Constructor<TeamMate> constructor = TeamMate.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            teamMate = constructor.newInstance();

            Field goalField = TeamMate.class.getDeclaredField("goal");
            goalField.setAccessible(true);
            Goal goal = Goal.builder()
                    .checkDays(new GoalCheckDays(this.checkDays))
                    .period(new GoalPeriod(startDate, endDate))
                    .build();
            goalField.set(teamMate, goal);

            Field progressField = TeamMate.class.getDeclaredField("progress");
            progressField.setAccessible(true);
            progressField.set(teamMate, new TeamMateProgress(workingDays, 0));
        } catch (NoSuchFieldException | IllegalAccessException  | NoSuchMethodException
                 | InvocationTargetException | InstantiationException e) {
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
                           int checkDays,
                           int workingDays,
                           List<String> teamMateNames) {
        this(id, category, title, startDate, endDate, appointmentTime, checkDays, workingDays);
        this.teamMateNames = teamMateNames;
    }
}
