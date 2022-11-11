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
    private TeamMateStatus status;
    @NotNull @Embedded
    private TeamMateProgress progress;
    private LocalDate lastUploadDay;

    public TeamMate(long userId) {
        this.userId = userId;
        this.status = TeamMateStatus.WAITING;
        this.progress = new TeamMateProgress();
    }

    void setGoal(Goal goal) {
        this.goal = goal;
        this.progress = new TeamMateProgress();
    }

    public void updateUploadedDate() {
        lastUploadDay = LocalDate.now();
    }

    public void applyInviteReject() {
        status = TeamMateStatus.REJECT;
    }

    public void plusWorkingDay() {
        progress.plusWorkingDay();
    }

    public void minusWorkingDay() {
        progress.minusWorkingDay();
    }

    public int getWorkingDays() {
        return progress.getWorkingDays();
    }

    public int getHookyDays() {
        return progress.getHookyDays();
    }

    // TODO: 2022/11/03 외부에서 ongoingGoalCount를 받는 게 맞을지
    public void initiateGoal(int ongoingGoalCount) {
        if(!goal.isInviteable()) throw new UnInviteableGoalException();
        changeToOngoingStatus(ongoingGoalCount);
        progress.setInitialProgress(goal.progressedWorkingDaysCount());
    }

    public String getSchedule(List<LocalDate> uploadedDates) {
        return goal.getPeriod().fromStartToEndDate()
                .map(date -> uploadedDates.contains(date) ? "1" : "0")
                .collect(Collectors.joining());
    }

    public void changeToWaitingStatus() {
        if(!goal.isInviteable()) throw new UnInviteableGoalException();
        status.inviteeStatusCheck();
        status = TeamMateStatus.WAITING;
    }

    public double calcProgressPercent() {
        return ProgressCalculator.calculate(progress.getWorkingDays(), goal.totalWorkingDaysCount());
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

    private void changeToOngoingStatus(int ongoingGoalCount) {
        if(ongoingGoalCount >= 10) throw new ExceedGoalLimitException();
        this.status = TeamMateStatus.ONGOING;
    }
}