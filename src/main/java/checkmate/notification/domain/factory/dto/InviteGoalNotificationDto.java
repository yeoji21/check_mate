package checkmate.notification.domain.factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/*
초대 보내는 사람 userId, nickname
goalTitle
초대 받는 사람 userId, teamMateId
 */
@Getter
@Builder
@AllArgsConstructor
public class InviteGoalNotificationDto {
    private long inviterUserId;
    private String inviterNickname;
    private String goalTitle;
    private long inviteeUserId;
    private long inviteeTeamMateId;
}
