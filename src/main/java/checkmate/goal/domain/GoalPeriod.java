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
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_GOAL_DATE);
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // TODO: 2024/01/14 contains today로 대체 가능?
    boolean isStarted() {
        return !startDate.isAfter(LocalDate.now());
    }

    public boolean contains(LocalDate date) {
        return !startDate.isAfter(date) && !endDate.isBefore(date);
    }

    double getProgressedPercent() {
        return ProgressCalculator.calculate(getPastDaysCount(), getTotalCount());
    }

    Stream<LocalDate> getFullPeriodStream() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    Stream<LocalDate> getUntilTodayPeriodStream() {
        return isStarted() ? startDate.datesUntil(LocalDate.now()) : Stream.empty();
    }

    private int getPastDaysCount() {
        return isStarted() ? (int) (startDate.datesUntil(LocalDate.now()).count()) : 0;
    }

    private int getTotalCount() {
        return (int) (startDate.datesUntil(endDate.plusDays(1)).count());
    }

}
