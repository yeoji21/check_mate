package checkmate.goal.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class GoalModifyRequest {
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;

    @Builder
    public GoalModifyRequest(LocalDate endDate,
                             LocalTime appointmentTime,
                             boolean timeReset) {
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.timeReset = timeReset;
    }
}
