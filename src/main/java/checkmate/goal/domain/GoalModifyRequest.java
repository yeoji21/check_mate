package checkmate.goal.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

// TODO: 2023/05/25 클래스명 변경 고려
// Request -> Event ?
@Getter
public class GoalModifyRequest {

    private LocalDate endDate;
    private LocalTime appointmentTime;
    private boolean timeReset;

    @Builder
    public GoalModifyRequest(LocalDate endDate,
        LocalTime appointmentTime,
        boolean timeReset) {
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.timeReset = timeReset;
    }
}
