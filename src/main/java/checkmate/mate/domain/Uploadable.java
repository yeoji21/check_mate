package checkmate.mate.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Uploadable {
    private boolean uploadable;
    private boolean uploaded;
    private boolean workingDay;
    private boolean timeOver;

    @Builder
    public Uploadable(boolean uploaded, boolean workingDay, boolean timeOver) {
        this.uploaded = uploaded;
        this.workingDay = workingDay;
        this.timeOver = timeOver;
        this.uploadable = !uploaded && workingDay && !timeOver;
    }
}
