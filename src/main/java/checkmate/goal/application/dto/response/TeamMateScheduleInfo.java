package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.TeamMate;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        TeamMate teamMate;
        try {
            Constructor<TeamMate> constructor = TeamMate.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            teamMate = constructor.newInstance();

            Field goalField = TeamMate.class.getDeclaredField("goal");
            goalField.setAccessible(true);
            goalField.set(teamMate, goal);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalSchedule = goal.getSchedule();
        this.teamMateSchedule = teamMate.getSchedule(uploadedDates);
        if(goalSchedule.length() != teamMateSchedule.length())
            throw new IllegalArgumentException();
    }
}
