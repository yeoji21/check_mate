package checkmate.mate.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class MateProgress {
    @Column(name = "check_day_count")
    private int checkDayCount;
    @Column(name = "skipped_day_count")
    private int skippedDayCount;

    public MateProgress(int checkDayCount, int skippedDayCount) {
        this.checkDayCount = checkDayCount;
        this.skippedDayCount = skippedDayCount;
    }

    void plusWorkingDay() {
        checkDayCount++;
    }

    void minusWorkingDay() {
        checkDayCount--;
    }
}
