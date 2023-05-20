package checkmate.mate.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.goal.domain.Goal;
import checkmate.user.domain.User;
import io.jsonwebtoken.lang.Assert;
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
@Table(name = "mate", indexes = {
        @Index(name = "goalId_userId_status_idx", columnList = "goal_id, user_id, status"),
        @Index(name = "userId_idx", columnList = "user_id"),
        @Index(name = "goalId_idx", columnList = "goal_id")
})
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
        this.status = MateStatus.CREATED;
        this.progress = new MateProgress();
        this.goal = goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    void toOngoingStatus() {
        goal.joinableCheck();
        status.initiateableCheck();
        status = MateStatus.ONGOING;
        progress = new MateProgress(goal.progressedWorkingDaysCount(), 0);
    }

    public void toWaitingStatus() {
        goal.joinableCheck();
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

    public void validatePostUploadable() {
        Assert.isTrue(getUploadable().isUploadable(), getUploadable().toString());
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

    public int getSkippedDays() {
        return progress.getSkippedDayCount();
    }
}