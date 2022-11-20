package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Uploadable;
import checkmate.goal.domain.*;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Getter
public class GoalDetailInfo {
    private long id;
    private List<TeamMateUploadInfo> teamMates;
    private GoalCategory category;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private String weekDays;
    private GoalStatus goalStatus;
    private boolean inviteable;
    private Uploadable uploadable;

    @QueryProjection
    public GoalDetailInfo(Goal goal,
                          TeamMate selector) {
        this.id = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.weekDays = CheckDaysConverter.toDays(goal.getCheckDays().intValue());
        this.goalStatus = goal.getStatus();
        this.inviteable = goal.isInviteable();
        this.uploadable = selector.getUploadable();
    }

    public void setTeamMates(List<TeamMateUploadInfo> teamMates) {
        this.teamMates = teamMates;
    }

    @Builder
    public GoalDetailInfo(Goal goal,
                          TeamMate selector,
                          List<TeamMateUploadInfo> otherTeamMates) {
        this(goal, selector);
        this.teamMates = otherTeamMates;
    }
}
