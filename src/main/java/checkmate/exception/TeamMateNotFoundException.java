package checkmate.exception;

import checkmate.exception.format.NotFoundException;
import checkmate.exception.format.ErrorCode;

public class TeamMateNotFoundException extends NotFoundException {
    public TeamMateNotFoundException() {
        super(ErrorCode.TEAM_MATE_NOT_FOUND);
    }
}
