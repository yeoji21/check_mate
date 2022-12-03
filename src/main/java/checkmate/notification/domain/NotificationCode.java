package checkmate.notification.domain;

import checkmate.exception.format.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCode implements BusinessCode {
    NOT_FOUND("NOTI-001", "해당 알림을 찾을 수 없습니다."),

    ;

    private final String code;
    private final String message;
}
