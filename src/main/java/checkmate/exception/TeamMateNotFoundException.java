package checkmate.exception;

import checkmate.exception.format.EntityNotFoundException;
import checkmate.exception.format.ErrorCode;

public class TeamMateNotFoundException extends EntityNotFoundException {
    public TeamMateNotFoundException() {
        super(ErrorCode.TEAM_MATE_NOT_FOUND);
    }
}
