package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Goal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "category", nullable = false)
    private GoalCategory category;
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;
    @Embedded
    @NotNull
    private GoalPeriod period;
    @Embedded
    @NotNull
    private GoalCheckDays checkDays;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status")
    private GoalStatus status;

    // TODO: 2023/02/27 목표 인증 조건으로 분리 가능
    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    @Builder
    public Goal(
        GoalCategory category,
        String title,
        GoalPeriod period,
        GoalCheckDays checkDays,
        LocalTime appointmentTime) {
        this.category = category;
        this.title = title;
        this.checkDays = checkDays;
        this.period = period;
        this.appointmentTime = appointmentTime;
        this.status = period.isInitiated() ? GoalStatus.ONGOING : GoalStatus.WAITING;
    }

    public int getSkippedDayLimit() {
        return getTotalCheckDayCount() / GoalPolicyConstants.GOAL_SKIP_LIMIT_PERCENT + 1;
    }

    public boolean isTodayCheckDay() {
        return period.isBelongToPeriod(LocalDate.now()) && checkDays.isDateCheckDayOfWeek(
            LocalDate.now());
    }

    public boolean isTimeOver() {
        if (this.appointmentTime == null) {
            return false;
        } else {
            return appointmentTime.isBefore(LocalTime.now());
        }
    }

    public Mate createMate(User user) {
        return new Mate(this, user);
    }

    public boolean isInviteable() {
        return period.getProgressedPercent() <= GoalPolicyConstants.INVITE_MAX_ACCEPTABLE_PERCENT;
    }

    public int getTotalCheckDayCount() {
        return (int) period.getFullPeriodStream()
            .filter(date -> checkDays.isDateCheckDayOfWeek(date)).count();
    }

    public int getProgressedCheckDayCount() {
        return (int) period.getUntilTodayPeriodStream()
            .filter(date -> checkDays.isDateCheckDayOfWeek(date)).count();
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }

    public void modify(GoalModifyEvent event) {
        checkModifyDeadline();
        modifyEndDate(event);
        modifyAppointmentTime(event);
    }

    private void checkModifyDeadline() {
        if (getModifiedDateTime() != null && isTodayModifyable()) {
            throw new BusinessException(ErrorCode.UPDATE_DURATION);
        }
    }

    private void modifyAppointmentTime(GoalModifyEvent event) {
        if (event.isTimeReset()) {
            appointmentTime = null;
        } else {
            if (event.getAppointmentTime() != null) {
                appointmentTime = event.getAppointmentTime();
            }
        }
    }

    private void modifyEndDate(GoalModifyEvent event) {
        if (event.getEndDate() != null) {
            validateEndDate(event.getEndDate());
            period = new GoalPeriod(period.getStartDate(), event.getEndDate());
        }
    }

    private boolean isTodayModifyable() {
        return getModifiedDateTime().plusDays(7).toLocalDate().isAfter(LocalDate.now());
    }

    private void validateEndDate(LocalDate newEndDate) {
        if (newEndDate != null && getEndDate().isAfter(newEndDate)) {
            throw new BusinessException(ErrorCode.INVALID_GOAL_DATE);
        }
    }

    public enum GoalCategory {
        EXERCISE,
        LIFESTYLE,
        READING,
        LEARNING,
        HOBBIES,
        ETC,
        ;
    }

    public enum GoalStatus {
        ONGOING, OVER, WAITING;
    }
}
