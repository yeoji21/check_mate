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
        this.startDate = startDate;
        this.endDate = endDate;
    }

    double getProgressedPercent() {
        return ProgressCalculator.calculate(
                (int)startDate.datesUntil(LocalDate.now()).count(),
                (int)startDate.datesUntil(endDate.plusDays(1)).count()
        );
    }

    Stream<LocalDate> fromStartToEndDate() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    Stream<LocalDate> fromStartToToday() {
        return startDate.datesUntil(LocalDate.now());
    }

    boolean checkDateRange() {
        return !startDate.isAfter(LocalDate.now()) && !endDate.isBefore(LocalDate.now());
    }
}
