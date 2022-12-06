package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCode{
    NOT_FOUND("NOTI-001"),
    PUSH_IO("NOTI-002")
    ;

    private final String code;
}
