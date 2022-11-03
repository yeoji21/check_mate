package checkmate.exception;

import checkmate.exception.format.EntityNotFoundException;
import checkmate.exception.format.ErrorCode;

public class NotificationNotFoundException extends EntityNotFoundException {
    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
