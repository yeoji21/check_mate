package checkmate.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    COMPLETE_GOAL("목표 수행 완료"),
    INVITE_GOAL("팀원 초대"),
    INVITE_GOAL_REPLY("초대 응답"),
    EXPULSION_GOAL("목표 퇴출 알림"),
    POST_UPLOAD("팀원의 목표인증"),
    INVITE_ACCEPT("초대 수락");

    private final String title;
}
