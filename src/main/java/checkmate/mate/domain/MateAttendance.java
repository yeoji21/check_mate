package checkmate.mate.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MateAttendance {

    @Column(name = "check_day_count")
    private int checkDayCount;
    @Column(name = "skipped_day_count")
    private int skippedDayCount;

    MateAttendance plusCheckDayCount() {
        return new MateAttendance(checkDayCount + 1, skippedDayCount);
    }

    MateAttendance minusCheckDayCount() {
        return new MateAttendance(checkDayCount - 1, skippedDayCount);
    }
}
