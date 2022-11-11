package checkmate.goal.application.dto.request;

import checkmate.goal.domain.GoalCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class GoalCreateCommand {
    private long userId;
    private GoalCategory category;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String checkDays;
    private LocalTime appointmentTime;
}
