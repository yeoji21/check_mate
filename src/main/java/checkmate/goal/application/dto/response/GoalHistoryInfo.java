package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.mate.domain.Mate;
import com.querydsl.core.annotations.QueryProjection;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;

@Getter
public class GoalHistoryInfo implements Serializable {

    private long goalId;
    private GoalCategory category;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private String checkDays;
    private double achievementRate;
    private List<String> mateNicknames;

    @QueryProjection
    public GoalHistoryInfo(Mate finder) {
        Goal goal = finder.getGoal();
        this.goalId = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.checkDays = CheckDaysConverter.toDays(goal.getCheckDays().intValue());
        this.achievementRate = finder.calcProgressPercent();
    }

    public void setMateNicknames(List<String> mateNicknames) {
        this.mateNicknames = mateNicknames;
    }
}
