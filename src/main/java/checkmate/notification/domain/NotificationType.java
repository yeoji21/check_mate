package checkmate.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    COMPLETE_GOAL("목표 수행 완료"),
    EXPULSION_GOAL("목표 퇴출 알림"),
    POST_UPLOAD("팀원의 목표인증"),
    INVITE_SEND("초대 요청"),
    INVITE_ACCEPT("초대 수락"),
    INVITE_REJECT("초대 거절");

    private final String title;
}
