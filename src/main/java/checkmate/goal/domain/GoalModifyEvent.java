package checkmate.goal.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class GoalModifyEvent {

    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;
}
