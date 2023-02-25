package checkmate.goal.presentation.dto;

import checkmate.goal.domain.GoalCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class GoalCreateDto {
    @NotNull(message = "category is blank")
    private GoalCategory category;
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

    @Builder
    public GoalCreateDto(GoalCategory category,
                         String title,
                         LocalDate startDate,
                         LocalDate endDate,
                         String checkDays,
                         LocalTime appointmentTime) {
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checkDays = checkDays;
        this.appointmentTime = appointmentTime;
    }
}
