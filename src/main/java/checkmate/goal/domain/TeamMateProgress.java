package checkmate.goal.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class TeamMateProgress {
    private Integer workingDays;
    private Integer hookyDays;

    public TeamMateProgress(Integer workingDays, Integer hookyDays) {
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
