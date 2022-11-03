package checkmate.goal.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TeamMateCalendarResponse {
    private LocalDate startDate;
    private String goalPeriod;
    private String teamMatePeriod;

    @Builder
    public TeamMateCalendarResponse(LocalDate startDate,
                                    String goalPeriod,
                                    String teamMatePeriod) {
        this.startDate = startDate;
        this.goalPeriod = goalPeriod;
        this.teamMatePeriod = teamMatePeriod;
    }
}
