package checkmate.goal.application.dto.response;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.mate.application.dto.response.MateUploadInfo;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;


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
        this.weekDays = goal.getCheckDays().toKorean();
        this.status = goal.getStatus();
        this.inviteable = goal.isInviteableProgress();
    }

    public void setMates(List<MateUploadInfo> mates) {
        this.mates = mates;
    }
}
