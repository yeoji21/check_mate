package checkmate.goal.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class GoalModifyCommand {
    private long goalId;
    private long userId;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;
}
