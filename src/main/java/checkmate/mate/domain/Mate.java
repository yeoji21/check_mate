package checkmate.mate.domain;

import static checkmate.exception.code.ErrorCode.INVALID_MATE_STATUS;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.common.util.ProgressCalculator;
import checkmate.exception.BusinessException;
import checkmate.exception.NotInviteableGoalException;
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
import javax.persistence.UniqueConstraint;
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
}, uniqueConstraints = {
    @UniqueConstraint(
        name = "unique_goalId_userId",
        columnNames = {"goal_id", "user_id"}
    )
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
    private MateAttendance attendance;
    private LocalDate lastUploadDate;

    public Mate(Goal goal, User user) {
        this.userId = user.getId();
        this.status = MateStatus.CREATED;
        this.attendance = new MateAttendance();
        this.goal = goal;
    }

    void acceptInvite() {
        if (goal.isInviteable()) {
            status = status.toOngoing();
            attendance = new MateAttendance(goal.getProgressedCheckDayCount(), 0);
            return;
        }
        throw NotInviteableGoalException.EXCEED_INVITEABLE_DATE;
    }

    public void receiveInvite() {
        if (goal.isInviteable()) {
            status = status.toWaiting();
            return;
        }
        throw NotInviteableGoalException.EXCEED_INVITEABLE_DATE;
    }

    public void rejectInvite() {
        status = status.toReject();
    }

    public double getAchievementPercent() {
        return ProgressCalculator.calculate(attendance.getCheckDayCount(),
            goal.getTotalCheckDayCount());
    }

    public void updateLastUpdateDate() {
        lastUploadDate = LocalDate.now();
    }

    public void plusCheckDayCount() {
        this.attendance = attendance.plusCheckDayCount();
    }

    public void minusCheckDayCount() {
        this.attendance = attendance.minusCheckDayCount();
    }

    public int getCheckDayCount() {
        return attendance.getCheckDayCount();
    }

    public int getSkippedDayCount() {
        return attendance.getSkippedDayCount();
    }

    public enum MateStatus {
        CREATED,
        WAITING,
        ONGOING,
        REJECT,
        OUT,
        SUCCESS;

        MateStatus toOngoing() {
            if (this == WAITING) {
                return ONGOING;
            }
            throw new BusinessException(INVALID_MATE_STATUS);
        }

        MateStatus toWaiting() {
            if (this == ONGOING || this == SUCCESS) {
                throw NotInviteableGoalException.ALREADY_IN_GOAL;
            } else if (this == WAITING) {
                throw NotInviteableGoalException.DUPLICATED_INVITE;
            }
            return WAITING;
        }

        public MateStatus toReject() {
            if (this == WAITING) {
                return REJECT;
            }
            throw new BusinessException(INVALID_MATE_STATUS);
        }
    }

}