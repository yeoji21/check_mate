package checkmate.goal.application.dto.request;

import checkmate.goal.domain.Goal.GoalCategory;
import java.time.DayOfWeek;
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
    DayOfWeek[] checkDays,
    LocalTime appointmentTime) {

}
