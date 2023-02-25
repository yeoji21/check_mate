package checkmate.mate.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.goal.domain.Goal;
import checkmate.user.domain.User;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mate")
@Entity
public class Mate extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MateStatus status;
    @NotNull
    @Embedded
    private MateProgress progress;
    private LocalDate lastUploadDate;

    public Mate(Goal goal, User user) {
        this.userId = user.getId();
        this.status = MateStatus.WAITING;
        this.progress = new MateProgress();
        this.goal = goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    void toOngoingStatus() {
        goal.inviteableCheck();
        status.initiateableCheck();
        status = MateStatus.ONGOING;
        progress = new MateProgress(goal.progressedWorkingDaysCount(), 0);
    }

    public void toWaitingStatus() {
        goal.inviteableCheck();
        status.inviteableCheck();
        status = MateStatus.WAITING;
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
        return lastUploadDate != null && lastUploadDate.isEqual(LocalDate.now());
    }

    public void updateUploadedDate() {
        lastUploadDate = LocalDate.now();
    }

    public void toRejectStatus() {
        status = MateStatus.REJECT;
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
}