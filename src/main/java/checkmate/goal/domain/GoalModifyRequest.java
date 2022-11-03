package checkmate.goal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class GoalModifyRequest {
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;
}
