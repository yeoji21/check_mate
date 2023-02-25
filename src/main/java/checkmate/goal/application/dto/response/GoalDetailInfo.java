package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalStatus;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Uploadable;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Getter
public class GoalDetailInfo {
    private long id;
    private List<MateUploadInfo> mates;
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
                          Mate selector) {
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

    public void setMates(List<MateUploadInfo> mates) {
        this.mates = mates;
    }
}
