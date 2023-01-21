package checkmate.goal.application.dto.request;

import checkmate.goal.domain.GoalCategory;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record GoalCreateCommand (
    long userId,
    GoalCategory category,
    String title,
    LocalDate startDate,
    LocalDate endDate,
    String checkDays,
    LocalTime appointmentTime){
}
