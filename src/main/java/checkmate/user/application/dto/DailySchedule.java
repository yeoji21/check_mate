package checkmate.user.application.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DailySchedule {

    private final LocalDate date;
    private final long goalId;
    private final boolean checked;
}
