package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
    @Getter(value = AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationCondition> conditions = new ArrayList<>();

    @Builder
    public Goal(GoalCategory category,
        String title,
        GoalPeriod period,
        GoalCheckDays checkDays,
        LocalTime appointmentTime) {
        this.category = category;
        this.title = title;
        this.checkDays = checkDays;
        this.period = period;
        this.appointmentTime = appointmentTime;
        this.status = period.isUninitiated() ? GoalStatus.WAITING : GoalStatus.ONGOING;
    }

    public void addCondition(VerificationCondition condition) {
        conditions.add(condition);
        condition.setGoal(this);
    }

    public boolean checkConditions(Post post) {
        return conditions.stream().allMatch(condition -> condition.satisfy(post));
    }

    public int getSkippedDayLimit() {
        return (int) (totalWorkingDaysCount() * 0.1 + 1);
    }

    public boolean isTodayWorkingDay() {
        return period.isBelongToPeriod(LocalDate.now()) && checkDays.isWorkingDay(LocalDate.now());
    }

    public boolean isTimeOver() {
        if (this.appointmentTime == null) {
            return false;
        } else {
            return appointmentTime.isBefore(LocalTime.now());
        }
    }

    public Mate join(User user) {
        joinableCheck();
        return new Mate(this, user);
    }

    public boolean isInviteable() {
        return GoalJoiningPolicy.progressedPercent(period.calcProgressedPercent());
    }

    public void joinableCheck() {
        if (!isInviteable()) {
            throw UnInviteableGoalException.EXCEED_GOAL_INVITEABLE_DATE;
        }
    }

    public String getSchedule() {
        return period.getGoalPeriodStream()
            .map(date -> checkDays.isWorkingDay(date) ? "1" : "0")
            .collect(Collectors.joining());
    }

    public String getSchedule(List<LocalDate> uploadedDates) {
        return period.getGoalPeriodStream()
            .map(date -> checkDays.isWorkingDay(date) && uploadedDates.contains(date) ? "1" : "0")
            .collect(Collectors.joining());
    }

    public int progressedWorkingDaysCount() {
        return checkDays.calcWorkingDayCount(period.getProgressedDateStream());
    }

    public int totalWorkingDaysCount() {
        return checkDays.calcWorkingDayCount(period.getGoalPeriodStream());
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }

    public void modify(GoalModifyEvent event) {
        validateModifyDeadline();
        modifyEndDate(event);
        modifyAppointmentTime(event);
    }

    private void validateModifyDeadline() {
        if (getModifiedDateTime() != null && lessThanModifyDeadline()) {
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

    private boolean lessThanModifyDeadline() {
        return getModifiedDateTime().plusDays(7).toLocalDate().isAfter(LocalDate.now());
    }

    private void validateEndDate(LocalDate newEndDate) {
        if (newEndDate != null && getEndDate().isAfter(newEndDate)) {
            throw new BusinessException(ErrorCode.INVALID_GOAL_DATE);
        }
    }
}
