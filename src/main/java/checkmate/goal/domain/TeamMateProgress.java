package checkmate.goal.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class TeamMateProgress {
    private int workingDays;
    private int hookyDays;

    public TeamMateProgress(int workingDays, int hookyDays) {
        this.workingDays = workingDays;
        this.hookyDays = hookyDays;
    }

    void plusWorkingDay() {
        workingDays++;
    }

    void minusWorkingDay() {
        workingDays--;
    }
}
