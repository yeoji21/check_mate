package checkmate.mate.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalStatus;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Uploadable;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
    private final double progress;

    private final List<MateUploadInfo> mates;

    public SpecifiedGoalDetailInfo(Mate mate,
                                   List<LocalDate> uploadedDates,
                                   List<MateUploadInfo> mates) {
        Goal goal = mate.getGoal();

        this.goalId = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.weekDays = CheckDaysConverter.toDays(goal.getCheckDays().intValue());
        this.status = goal.getStatus();
        this.inviteable = goal.isInviteable();
        this.goalSchedule = goal.getSchedule();
        this.mateSchedule = goal.getSchedule(uploadedDates);

        this.uploadable = mate.getUploadable();
        this.progress = mate.calcProgressPercent();

        this.mates = mates;
    }

}