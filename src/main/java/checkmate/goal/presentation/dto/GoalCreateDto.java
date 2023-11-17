package checkmate.goal.presentation.dto;

import checkmate.goal.domain.Goal;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class GoalCreateDto {

    @NotNull(message = "category is blank")
    private Goal.GoalCategory category;
    @NotBlank(message = "title is blank")
    @Size(max = 30, message = "title size must be less than 30 char")
    private String title;
    @NotNull(message = "startDate is null")
    private LocalDate startDate;
    @NotNull(message = "endDate is null")
    private LocalDate endDate;
    @NotBlank(message = "weekDays is blank")
    private String checkDays;
    private LocalTime appointmentTime;
}
