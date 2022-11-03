package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.exception.ExceedGoalLimitException;
import checkmate.exception.UnInviteableGoalException;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_mate")
@Entity
public class TeamMate extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_mate_id")
    private Long id;
    @NotNull @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @NotNull @Enumerated(EnumType.STRING)
    private TeamMateStatus teamMateStatus;
    @NotNull @Embedded
    private ProgressInfo progressInfo;
    private LocalDate lastUploadDay;

    public TeamMate(long userId) {
        this.userId = userId;
        this.teamMateStatus = TeamMateStatus.WAITING;
        this.progressInfo = new ProgressInfo();
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
        this.progressInfo = new ProgressInfo();
    }

    public void updateUploadedDate() {
        lastUploadDay = LocalDate.now();
    }

    public void applyInviteAgree(int ongoingGoalCount) {
        if(!goal.isInviteable()) throw new UnInviteableGoalException();
        changeToOngoingStatus(ongoingGoalCount);
        progressInfo.setInitialProgress(goal.progressedWorkingDaysCount());
    }

    public void applyInviteReject() {
        teamMateStatus = TeamMateStatus.REJECT;
    }

    public void plusWorkingDay() {
        progressInfo.plusWorkingDay();
    }

    public void minusWorkingDay() {
        progressInfo.minusWorkingDay();
    }

    public int getWorkingDays() {
        return progressInfo.getWorkingDays();
    }

    public int getHookyDays() {
        return progressInfo.getHookyDays();
    }

    // TODO: 2022/11/03 이상함
    public void changeToOngoingStatus(int ongoingGoalCount) {
        if(ongoingGoalCount >= 10) throw new ExceedGoalLimitException();
        this.teamMateStatus = TeamMateStatus.ONGOING;
    }

    public void changeToWaitingStatus() {
        if(!goal.isInviteable()) throw new UnInviteableGoalException();

        teamMateStatus.inviteeStatusCheck();
        teamMateStatus = TeamMateStatus.WAITING;
    }

    public void changeToSuccessStatus() {
        if(getWorkingDays() == 0) return;
        teamMateStatus = TeamMateStatus.SUCCESS;
    }

    public double calcProgressPercent() {
        return ProgressCalculator.calculate(progressInfo.getWorkingDays(), goal.totalWorkingDaysCount());
    }

    public Uploadable getUploadable() {
        return Uploadable.builder()
                .uploaded(isUploaded())
                .timeOver(goal.isTimeOver())
                .workingDay(goal.isTodayWorkingDay())
                .build();
    }

    private boolean isUploaded() {
        return (lastUploadDay != null && lastUploadDay.isEqual(LocalDate.now()));
    }
}