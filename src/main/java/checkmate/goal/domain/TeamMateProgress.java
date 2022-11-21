package checkmate.goal.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class TeamMateProgress {
    @Column(name = "check_day_count")
    private int checkDayCount;
    @Column(name = "skipped_day_count")
    private int skippedDayCount;

    public TeamMateProgress(int checkDayCount, int skippedDayCount) {
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
