package checkmate.mate.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.goal.domain.GoalScheduler;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Uploadable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;

@Getter
public class SpecifiedGoalDetailInfo {

    private final long goalId;
    private final GoalCategory category;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime appointmentTime;
    private final String weekDays;
    private final GoalStatus status;
    private final boolean inviteable;
    private final Uploadable uploadable;
    private final String goalSchedule;
    private final String mateSchedule;
    private final double achievementPercent;
    private final List<MateUploadInfo> mates;

    public SpecifiedGoalDetailInfo(Mate mate,
        List<LocalDate> uploadedDates,
        List<MateUploadInfo> mateUploadInfo) {
        Goal goal = mate.getGoal();

        this.goalId = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.weekDays = goal.getCheckDays().toKorean();
        this.status = goal.getStatus();
        this.inviteable = goal.isInviteableProgress();
        this.goalSchedule = GoalScheduler.getTotalSchedule(goal.getPeriod(),
            goal.getCheckDays());
        this.mateSchedule = GoalScheduler.getCheckedSchedule(goal.getPeriod(),
            goal.getCheckDays(), uploadedDates);
        this.uploadable = new Uploadable(mate);
        this.achievementPercent = mate.getAchievementPercent();
        this.mates = mateUploadInfo;
    }

}
