package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.mate.domain.Mate;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class GoalHistoryInfo {
    private long goalId;
    private GoalCategory category;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private String checkDays;
    private double achievementRate;
    private List<String> teamMateNicknames;

    public GoalHistoryInfo(Mate finder, List<String> teamMateNicknames) {
        Goal goal = finder.getGoal();
        this.goalId = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.checkDays = CheckDaysConverter.toDays(goal.getCheckDays().intValue());
        this.achievementRate = finder.calcProgressPercent();
        this.teamMateNicknames = teamMateNicknames;
    }
}
