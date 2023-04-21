package checkmate.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationAttributeKey {
    GOAL_ID("goalId"),
    MATE_ID("mateId"),
    USER_ID("userId");

    private final String key;
}
