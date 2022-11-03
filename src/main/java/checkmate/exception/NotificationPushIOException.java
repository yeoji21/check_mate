package checkmate.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotificationPushIOException extends RuntimeException {
    public NotificationPushIOException(Throwable cause) {
        super(cause);
    }
}
