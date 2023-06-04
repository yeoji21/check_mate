package checkmate.goal.application.dto.request;

import checkmate.goal.domain.Goal.GoalCategory;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record GoalCreateCommand(
    long userId,
    GoalCategory category,
    String title,
    LocalDate startDate,
    LocalDate endDate,
    String checkDays,
    LocalTime appointmentTime) {

}
