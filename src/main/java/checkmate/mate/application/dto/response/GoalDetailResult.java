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
public class GoalDetailResult {
    private final long id;
    private final GoalCategory category;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime appointmentTime;
    private final String weekDays;
    private final GoalStatus goalStatus;
    private final boolean inviteable;
    private final String goalSchedule;

    private final Uploadable uploadable;
    private final String mateSchedule;
    private final double progress;

    private final List<MateUploadInfo> mates;

    public GoalDetailResult(Mate mate,
                            List<LocalDate> uploadedDates,
                            List<MateUploadInfo> mates) {
        Goal goal = mate.getGoal();

        this.id = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.weekDays = CheckDaysConverter.toDays(goal.getCheckDays().intValue());
        this.goalStatus = goal.getStatus();
        this.inviteable = goal.isInviteable();
        this.goalSchedule = goal.getSchedule();
        this.mateSchedule = goal.getSchedule(uploadedDates);

        this.uploadable = mate.getUploadable();
        this.progress = mate.calcProgressPercent();

        this.mates = mates;
    }

}
