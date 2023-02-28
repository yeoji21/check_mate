package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalStatus;
import checkmate.mate.application.dto.response.MateUploadInfo;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Getter
public class GoalDetailInfo {
    private long goalId;
    private List<MateUploadInfo> mates;
    private GoalCategory category;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private String weekDays;
    private GoalStatus status;
    private boolean inviteable;

    @QueryProjection
    public GoalDetailInfo(Goal goal) {
        this.goalId = goal.getId();
        this.category = goal.getCategory();
        this.title = goal.getTitle();
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.appointmentTime = goal.getAppointmentTime();
        this.weekDays = CheckDaysConverter.toDays(goal.getCheckDays().intValue());
        this.status = goal.getStatus();
        this.inviteable = goal.isInviteable();
    }

    public void setMates(List<MateUploadInfo> mates) {
        this.mates = mates;
    }
}
