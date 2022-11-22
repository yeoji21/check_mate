package checkmate.goal.application.dto.request;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record GoalModifyCommand(
        long goalId,
        long userId,
        LocalDate endDate,
        LocalTime appointmentTime,
        boolean timeReset) {
}
