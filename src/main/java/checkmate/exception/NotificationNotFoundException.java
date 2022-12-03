package checkmate.exception;

import checkmate.exception.format.NotFoundException;
import checkmate.exception.format.ErrorCode;

public class NotificationNotFoundException extends NotFoundException {
    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
