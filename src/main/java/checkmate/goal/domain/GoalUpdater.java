package checkmate.goal.domain;

import checkmate.exception.InvalidDateRangeException;
import checkmate.exception.UpdateDurationException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

// TODO: 2022/08/18 개선 해야 함
@RequiredArgsConstructor
public class GoalUpdater {
    private final GoalModifyRequest request;

    public void update(Goal goal) {
        validate(goal);

        if (request.getEndDate() != null)
            goal.extendEndDate(request.getEndDate());

        if (request.isTimeReset()) goal.updateAppointmentTime(null);
        else {
            if (request.getAppointmentTime() != null)
                goal.updateAppointmentTime(request.getAppointmentTime());
        }
    }

    private void validate(Goal goal) {
        modifiedDateCheck(goal);
        endDateCheck(goal);
    }

    private void endDateCheck(Goal goal) {
        if (request.getEndDate() != null && goal.getEndDate().isAfter(request.getEndDate()))
            throw new InvalidDateRangeException();
    }

    private void modifiedDateCheck(Goal goal) {
        if(goal.getModifiedDate() != null && goal.getModifiedDate().plusDays(7).toLocalDate().isAfter(LocalDate.now()))
            throw new UpdateDurationException();
    }
}
