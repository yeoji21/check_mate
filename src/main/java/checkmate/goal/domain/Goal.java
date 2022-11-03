package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.UnInviteableGoalException;
import checkmate.post.domain.Post;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Entity
public class Goal extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="goal_id")
    private Long id;
    @Enumerated(EnumType.STRING) @NotNull
    @Column(name = "category", nullable = false)
    private GoalCategory category;
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;
    @Embedded @NotNull
    public GoalPeriod period;
    @Embedded @NotNull
    private WeekDays weekDays;
    @Enumerated(EnumType.STRING) @NotNull
    private GoalStatus goalStatus;
    // TODO: 2022/07/22 aggregate 분리 -> 후순위
    @Embedded
    public Team team;
    @Column(name = "appointment_time", nullable = true)
    private LocalTime appointmentTime;

    @Getter(value = AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationCondition> conditions = new ArrayList<>();

    @Builder
    public Goal(GoalCategory category,
                String title,
                LocalDate startDate,
                LocalDate endDate,
                String weekDays,
                LocalTime appointmentTime) {
        this.category = category;
        this.title = title;
        this.weekDays = new WeekDays(weekDays);
        this.period = new GoalPeriod(startDate, endDate);
        this.appointmentTime = appointmentTime;
        this.goalStatus = GoalStatus.ONGOING;
        team = new Team();
    }

    public void addTeamMate(TeamMate teamMate) {
        team.add(teamMate);
        teamMate.setGoal(this);
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
        return period.checkDateRange() && weekDays.isWorkingDay(LocalDate.now());
    }

    public boolean isTimeOver() {
        if(this.appointmentTime == null) return false;
        else return appointmentTime.isBefore(LocalTime.now());
    }

    public boolean isInviteable() {
        return period.getProgressedPercent() <= 25.0;
    }

    public List<TeamMate> getTeam() {
        return team.getTeamMates();
    }

    void extendEndDate(LocalDate endDate) {
        period = new GoalPeriod(period.getStartDate(), endDate);
    }

    void updateAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void includeNewTeamMate(long userId) {
        if(!isInviteable()) throw new UnInviteableGoalException();
        addTeamMate(new TeamMate(userId));
    }

    public String getCalendar() {
        return weekDays.getWorkingDays(period.fromStartToEndDate());
    }

    public int progressedWorkingDaysCount() {
        return weekDays.calcWorkingDayCount(period.fromStartToToday());
    }

    int totalWorkingDaysCount() {
        return weekDays.calcWorkingDayCount(period.fromStartToEndDate());
    }

    public LocalDate getStartDate() {
        return period.getStartDate();
    }

    public LocalDate getEndDate() {
        return period.getEndDate();
    }
}
