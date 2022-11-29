package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.UnInviteableGoalException;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Entity
public class Goal extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Enumerated(EnumType.STRING) @NotNull
    @Column(name = "category", nullable = false)
    private GoalCategory category;
    @NotNull @Column(name = "title", nullable = false)
    private String title;
    @Embedded @NotNull
    public GoalPeriod period;
    @Embedded @NotNull
    private GoalCheckDays checkDays;
    @Enumerated(EnumType.STRING) @NotNull
    @Column(name = "status")
    private GoalStatus status;
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

    public boolean verifyConditions(Post post) {
        if(post.isChecked()) return false;
        return conditions.stream()
                .allMatch(condition -> condition.satisfy(post));
    }

    public int getHookyDayLimit() {
        return (int) (totalWorkingDaysCount() * 0.1 + 1);
    }

    public boolean isTodayWorkingDay() {
        return period.checkDateRange() && checkDays.isWorkingDay(LocalDate.now());
    }

    public boolean isTimeOver() {
        if(this.appointmentTime == null) return false;
        else return appointmentTime.isBefore(LocalTime.now());
    }

    void extendEndDate(LocalDate endDate) {
        period = new GoalPeriod(period.getStartDate(), endDate);
    }

    void updateAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    /*
     User가 Goal에 참여
     생성된 TeamMate의 status는 waiting이므로 목표 인증을 시작하지 않은 상태
     */
    public TeamMate join(User user) {
        inviteableCheck();
        return new TeamMate(this, user);
    }

    public boolean isInviteable() {
        return GoalJoiningPolicy.progressedPercent(period.calcProgressedPercent());
    }

    public void inviteableCheck() {
        if (!isInviteable()) throw new UnInviteableGoalException();
    }

    public String getSchedule() {
        return period.fromStartToEndDate()
                .map(date -> checkDays.isWorkingDay(date) ? "1" : "0")
                .collect(Collectors.joining());
    }

    public int progressedWorkingDaysCount() {
        return checkDays.calcWorkingDayCount(period.fromStartToToday());
    }

    int totalWorkingDaysCount() {
        return checkDays.calcWorkingDayCount(period.fromStartToEndDate());
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }
}
