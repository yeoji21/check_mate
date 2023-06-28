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
        this.startDate = startDate;
        this.endDate = endDate;
        if (isIncorrectPeriod()) {
            throw new BusinessException(ErrorCode.INVALID_GOAL_DATE);
        }
    }

    double getProgressedPercent() {
        return ProgressCalculator.calculate(getPastDaysCount(), getTotalCount());
    }

    Stream<LocalDate> getFullPeriodStream() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    Stream<LocalDate> getUntilTodayPeriodStream() {
        return isInitiated() ? startDate.datesUntil(LocalDate.now()) : Stream.empty();
    }

    boolean isBelongToPeriod(LocalDate date) {
        return !startDate.isAfter(date) && !endDate.isBefore(date);
    }

    boolean isInitiated() {
        return !startDate.isAfter(LocalDate.now());
    }

    private int getPastDaysCount() {
        return isInitiated() ? (int) (startDate.datesUntil(LocalDate.now()).count()) : 0;
    }

    private int getTotalCount() {
        return (int) (startDate.datesUntil(endDate.plusDays(1)).count());
    }

    private boolean isIncorrectPeriod() {
        return startDate.isAfter(endDate);
    }
}
