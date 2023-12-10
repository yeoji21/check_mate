package checkmate.mate.domain;

import java.time.LocalDate;
import lombok.Getter;


@Getter
public class Uploadable {

    private boolean uploadable;
    private boolean uploaded;
    private boolean checkDay;
    private boolean timeOver;

    public Uploadable(Mate mate) {
        this.uploaded = isTodayUploaded(mate);
        this.timeOver = mate.getGoal().isAppointmentTimeOver();
        this.checkDay = mate.getGoal().isTodayCheckDay();
        this.uploadable = !uploaded && checkDay && !timeOver;
    }

    private boolean isTodayUploaded(Mate mate) {
        return mate.getLastUploadDate() != null &&
            mate.getLastUploadDate().isEqual(LocalDate.now());
    }

    @Override
    public String toString() {
        return "{ uploadable = " + uploadable +
            ", uploaded = " + uploaded +
            ", checkDay = " + checkDay +
            ", timeOver = " + timeOver +
            " }";
    }
}
