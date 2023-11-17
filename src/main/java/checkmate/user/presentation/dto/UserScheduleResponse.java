package checkmate.user.presentation.dto;


import checkmate.user.application.dto.DailySchedule;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserScheduleResponse {

    private final LocalDate requestDate;
    private final List<DailySchedule> schedule;
}
