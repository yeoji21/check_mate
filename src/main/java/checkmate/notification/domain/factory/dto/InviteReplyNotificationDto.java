package checkmate.notification.domain.factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/*
- 초대 받은 유저의 닉네임, userId
- 초대를 보낸 유저의 userId
- goalId
- goal title
- accept
 */

@Getter
@Builder
@AllArgsConstructor
public class InviteReplyNotificationDto {
    private long inviteeUserId;
    private String inviteeNickname;
    private long goalId;
    private String goalTitle;
    private long inviterUserId;
    private boolean accept;
}
