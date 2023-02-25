package checkmate.goal.application.dto.response;

import checkmate.goal.domain.*;
import checkmate.mate.application.dto.response.MateUploadInfo;
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
    private final String teamMateSchedule;
    private final double progress;

    private final List<MateUploadInfo> teamMates;

    public GoalDetailResult(TeamMate teamMate,
                            List<LocalDate> uploadedDates,
                            List<MateUploadInfo> teamMates) {
        Goal goal = teamMate.getGoal();

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
        this.teamMateSchedule = goal.getSchedule(uploadedDates);

        this.uploadable = teamMate.getUploadable();
        this.progress = teamMate.calcProgressPercent();

        this.teamMates = teamMates;
    }

}
