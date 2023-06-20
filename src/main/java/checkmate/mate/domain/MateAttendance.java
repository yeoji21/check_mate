package checkmate.mate.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class MateAttendance {

    @Column(name = "check_day_count")
    private int checkDayCount;
    @Column(name = "skipped_day_count")
    private int skippedDayCount;

    public MateAttendance(int checkDayCount, int skippedDayCount) {
        this.checkDayCount = checkDayCount;
        this.skippedDayCount = skippedDayCount;
    }

    void plusCheckDayCount() {
        checkDayCount++;
    }

    void minusCheckDayCount() {
        checkDayCount--;
    }
}
