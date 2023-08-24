package checkmate.goal.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class GoalModifyDto {

    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;
}
