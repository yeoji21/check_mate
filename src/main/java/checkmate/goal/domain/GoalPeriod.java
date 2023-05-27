package checkmate.goal.domain;

import checkmate.common.util.ProgressCalculator;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import java.time.LocalDate;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalPeriod {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public GoalPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    double calcProgressedPercent() {
        return ProgressCalculator.calculate(getProgressedCount(), getTotalCount());
    }

    Stream<LocalDate> getGoalPeriodStream() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    Stream<LocalDate> getProgressedDateStream() {
        return isUninitiated() ? Stream.empty() : startDate.datesUntil(LocalDate.now());
    }

    boolean isBelongToPeriod(LocalDate date) {
        return !isUninitiated() && !endDate.isBefore(date);
    }

    boolean isUninitiated() {
        return startDate.isAfter(LocalDate.now());
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_GOAL_DATE);
        }
    }

    private int getProgressedCount() {
        return isUninitiated() ? 0 : (int) (startDate.datesUntil(LocalDate.now()).count());
    }

    private int getTotalCount() {
        return (int) (startDate.datesUntil(endDate.plusDays(1)).count());
    }
}
