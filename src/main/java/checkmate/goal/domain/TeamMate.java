package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.exception.ExceedGoalLimitException;
import checkmate.exception.UnInviteableGoalException;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


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
    private TeamMateProgress teamMateProgress;
    private LocalDate lastUploadDay;

    public TeamMate(long userId) {
        this.userId = userId;
        this.teamMateStatus = TeamMateStatus.WAITING;
        this.teamMateProgress = new TeamMateProgress();
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
        this.teamMateProgress = new TeamMateProgress();
    }

    public void updateUploadedDate() {
        lastUploadDay = LocalDate.now();
    }

    public void applyInviteAgree(int ongoingGoalCount) {
        if(!goal.isInviteable()) throw new UnInviteableGoalException();
        changeToOngoingStatus(ongoingGoalCount);
        teamMateProgress.setInitialProgress(goal.progressedWorkingDaysCount());
    }

    public void applyInviteReject() {
        teamMateStatus = TeamMateStatus.REJECT;
    }

    public void plusWorkingDay() {
        teamMateProgress.plusWorkingDay();
    }

    public void minusWorkingDay() {
        teamMateProgress.minusWorkingDay();
    }

    public int getWorkingDays() {
        return teamMateProgress.getWorkingDays();
    }

    public int getHookyDays() {
        return teamMateProgress.getHookyDays();
    }

    // TODO: 2022/11/03 이상함
    public void changeToOngoingStatus(int ongoingGoalCount) {
        if(ongoingGoalCount >= 10) throw new ExceedGoalLimitException();
        this.teamMateStatus = TeamMateStatus.ONGOING;
    }

    public String getSchedule(List<LocalDate> uploadedDates) {
        return goal.getPeriod().fromStartToEndDate()
                .map(date -> uploadedDates.contains(date) ? "1" : "0")
                .collect(Collectors.joining());
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
        return ProgressCalculator.calculate(teamMateProgress.getWorkingDays(), goal.totalWorkingDaysCount());
    }

    public Uploadable getUploadable() {
        return Uploadable.builder()
                .uploaded(isUploaded())
                .timeOver(goal.isTimeOver())
                .workingDay(goal.isTodayWorkingDay())
                .build();
    }

    public boolean isUploaded() {
        return (lastUploadDay != null && lastUploadDay.isEqual(LocalDate.now()));
    }
}