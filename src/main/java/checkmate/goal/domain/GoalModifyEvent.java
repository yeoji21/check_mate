package checkmate.goal.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;


@Getter
public class GoalModifyEvent {

    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;

    @Builder
    public GoalModifyEvent(LocalDate endDate,
        LocalTime appointmentTime,
        boolean timeReset) {
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.timeReset = timeReset;
    }
}
