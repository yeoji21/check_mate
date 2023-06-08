package checkmate.mate.domain;

import static checkmate.exception.code.ErrorCode.INVALID_MATE_STATUS;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;
import checkmate.goal.domain.Goal;
import checkmate.user.domain.User;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

    // TODO: 2023/06/08 메소드명 변경 고려
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
        return ProgressCalculator.calculate(progress.getCheckDayCount(),
            goal.totalWorkingDaysCount());
    }

    public Uploadable getUploadable() {
        return this.new Uploadable();
    }

    public void updatePostUploadedDate() {
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

    public enum MateStatus {
        CREATED,
        WAITING,
        ONGOING,
        REJECT,
        OUT,
        SUCCESS;

        void inviteableCheck() {
            if (this == ONGOING || this == SUCCESS) {
                throw UnInviteableGoalException.ALREADY_IN_GOAL;
            } else if (this == WAITING) {
                throw UnInviteableGoalException.DUPLICATED_INVITE_REQUEST;
            }
        }

        void initiateableCheck() {
            if (this != WAITING) {
                throw new BusinessException(INVALID_MATE_STATUS);
            }
        }
    }

    @Getter
    public class Uploadable {

        private boolean uploadable;
        private boolean uploaded;
        private boolean workingDay;
        private boolean timeOver;

        Uploadable() {
            this.uploaded = lastUploadDate != null && lastUploadDate.isEqual(LocalDate.now());
            this.timeOver = goal.isTimeOver();
            this.workingDay = goal.isTodayWorkingDay();
            this.uploadable = !uploaded && workingDay && !timeOver;
        }

        @Override
        public String toString() {
            return "{ uploadable = " + uploadable +
                ", uploaded = " + uploaded +
                ", workingDay = " + workingDay +
                ", timeOver = " + timeOver +
                " }";
        }
    }
}