package checkmate.goal.domain;

import checkmate.common.util.ProgressCalculator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.util.stream.Stream;

@Getter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GoalPeriod {
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public GoalPeriod(LocalDate startDate, LocalDate endDate) {
        if(startDate.isAfter(endDate))
            throw new IllegalArgumentException("올바르지 않은 날짜입니다.");
        this.startDate = startDate;
        this.endDate = endDate;
    }

    double calcProgressedPercent() {
        return ProgressCalculator.calculate(
                isUninitiated() ? 0 : (int)(startDate.datesUntil(LocalDate.now()).count()),
                (int)(startDate.datesUntil(endDate.plusDays(1)).count())
        );
    }

    Stream<LocalDate> fromStartToEndDate() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    Stream<LocalDate> fromStartToToday() {
        return isUninitiated() ?
                Stream.empty() : startDate.datesUntil(LocalDate.now());
    }

    boolean checkDateRange() {
        return !isUninitiated() && !endDate.isBefore(LocalDate.now());
    }

    boolean isUninitiated() {
        return startDate.isAfter(LocalDate.now());
    }
}
