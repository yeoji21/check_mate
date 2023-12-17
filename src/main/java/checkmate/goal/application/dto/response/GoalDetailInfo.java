package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.Goal.GoalStatus;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

// TODO: 2023/12/17 네이밍 변경 필요
@Getter
public class GoalDetailInfo {

    private long goalId;
    private List<MateInfo> mates;
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
        this.weekDays = CheckDaysConverter.toKorean(goal.getCheckDays());
        this.status = goal.getStatus();
        this.inviteable = goal.isInviteable();
    }

    public void setMates(List<MateInfo> mates) {
        this.mates = mates;
    }

    @Getter
    public static class MateInfo {

        private long userId;
        private long mateId;
        private String nickname;
        private boolean uploaded;

        @QueryProjection
        @Builder
        public MateInfo(long mateId,
            long userId,
            LocalDate lastUploadDate,
            String nickname) {
            this.mateId = mateId;
            this.userId = userId;
            this.uploaded = lastUploadDate != null && lastUploadDate.isEqual(LocalDate.now());
            this.nickname = nickname;
        }
    }
}
