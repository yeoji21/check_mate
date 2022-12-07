package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.user.domain.User;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(name="id")
    private Long id;
    @NotNull @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TeamMateStatus status;
    @NotNull @Embedded
    private TeamMateProgress progress;
    private LocalDate lastUploadDate;

    TeamMate(Goal goal, User user) {
        this.userId = user.getId();
        this.status = TeamMateStatus.WAITING;
        this.progress = new TeamMateProgress();
        this.goal = goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public void updateUploadedDate() {
        lastUploadDate = LocalDate.now();
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
        return progress.getCheckDayCount();
    }

    public int getHookyDays() {
        return progress.getSkippedDayCount();
    }

    // TODO: 2022/11/03 외부에서 ongoingGoalCount를 받는 게 맞을지
    public void initiateGoal(int ongoingGoalCount) {
        goal.inviteableCheck();
        GoalJoiningPolicy.ongoingGoalCount(ongoingGoalCount);
        this.status = TeamMateStatus.ONGOING;
        progress = new TeamMateProgress(goal.progressedWorkingDaysCount(), 0);
    }

    public String getSchedule(List<LocalDate> uploadedDates) {
        return goal.getPeriod().fromStartToEndDate()
                .map(date -> uploadedDates.contains(date) ? "1" : "0")
                .collect(Collectors.joining());
    }

    public void changeToWaitingStatus() {
        goal.inviteableCheck();
        status.inviteableCheck();
        status = TeamMateStatus.WAITING;
    }

    public double calcProgressPercent() {
        return ProgressCalculator.calculate(progress.getCheckDayCount(), goal.totalWorkingDaysCount());
    }

    public Uploadable getUploadable() {
        return Uploadable.builder()
                .uploaded(isUploaded())
                .timeOver(goal.isTimeOver())
                .workingDay(goal.isTodayWorkingDay())
                .build();
    }

    public boolean isUploaded() {
        return (lastUploadDate != null && lastUploadDate.isEqual(LocalDate.now()));
    }

}