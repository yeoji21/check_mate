package checkmate.goal.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class GoalModifyDto {
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;

    @Builder
    public GoalModifyDto(LocalDate endDate,
                         LocalTime appointmentTime,
                         boolean timeReset) {
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.timeReset = timeReset;
    }
}
