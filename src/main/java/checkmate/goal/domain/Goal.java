package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.BusinessException;
import checkmate.exception.NotInviteableGoalException;
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
        this.status = period.isStarted() ? GoalStatus.ONGOING : GoalStatus.WAITING;
    }

    public int getLimitOfSkippedDay() {
        return getTotalCheckDayCount() / GoalPolicyConstants.GOAL_SKIP_LIMIT_PERCENT + 1;
    }

    public boolean isTodayCheckDay() {
        return period.contains(LocalDate.now()) &&
            checkDays.isCheckDay(LocalDate.now());
    }

    public boolean isAppointmentTimeOver() {
        if (this.appointmentTime == null) {
            return false;
        }
        return appointmentTime.isBefore(LocalTime.now());
    }

    public Mate createMate(User user) {
        return new Mate(this, user);
    }

    public boolean isInviteable() {
        return period.getProgressedPercent()
            <= GoalPolicyConstants.INVITE_ACCEPTABLE_PROGRESSED_PERCENT_LIMIT;
    }

    public void checkJoinable() {
        if (!isInviteable()) {
            throw NotInviteableGoalException.EXCEED_INVITEABLE_DATE;
        }
    }

    public int getProgressedCheckDayCount() {
        return (int) period.getUntilTodayPeriodStream()
            .filter(date -> checkDays.isCheckDay(date))
            .count();
    }

    public int getTotalCheckDayCount() {
        return (int) period.getFullPeriodStream()
            .filter(date -> checkDays.isCheckDay(date))
            .count();
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }

    public void modify(GoalModifyEvent event) {
        if (isModifiedWithin7Days()) {
            throw new BusinessException(ErrorCode.UPDATE_DURATION);
        }
        modifyEndDate(event);
        modifyAppointmentTime(event);
    }

    private void modifyEndDate(GoalModifyEvent event) {
        if (event.getEndDate() == null) {
            return;
        }
        if (isEarlierThanEndDate(event.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_GOAL_DATE);
        }
        period = new GoalPeriod(period.getStartDate(), event.getEndDate());
    }

    private void modifyAppointmentTime(GoalModifyEvent event) {
        if (event.isTimeReset()) {
            appointmentTime = null;
            return;
        }
        if (event.getAppointmentTime() != null) {
            appointmentTime = event.getAppointmentTime();
        }
    }

    private boolean isEarlierThanEndDate(LocalDate endDateToModify) {
        return endDateToModify != null && endDateToModify.isBefore(getEndDate());
    }

    private boolean isModifiedWithin7Days() {
        return getModifiedDateTime() != null &&
            getModifiedDateTime().plusDays(7).toLocalDate().isAfter(LocalDate.now());
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
