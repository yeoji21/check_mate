package checkmate.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType{
    COMPLETE_GOAL,
    INVITE_GOAL,
    INVITE_GOAL_REPLY,
    EXPULSION_GOAL,
    POST_UPLOAD;
}
