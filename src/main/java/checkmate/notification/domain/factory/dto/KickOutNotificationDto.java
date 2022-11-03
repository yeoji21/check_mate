package checkmate.notification.domain.factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/*
- goalTitle
- teamMateId
- userId
 */
@Getter
@Builder
@AllArgsConstructor
public class KickOutNotificationDto {
    private long userId;
    private long teamMateId;
    private String goalTitle;
}
