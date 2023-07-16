package checkmate.notification.domain.factory.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.Getter;

@Getter
public class PostUploadNotificationDto implements NotificationCreateDto {

    private final long uploaderUserId;
    private final String uploaderNickname;
    private final long goalId;
    private final String goalTitle;
    private List<Long> mateUserIds;

    @QueryProjection
    public PostUploadNotificationDto(long uploaderUserId,
        String uploaderNickname,
        long goalId,
        String goalTitle) {
        this.uploaderUserId = uploaderUserId;
        this.uploaderNickname = uploaderNickname;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
    }

    // TODO: 2023/07/04 개선
    public void setMateUserIds(List<Long> mateUserIds) {
        this.mateUserIds = mateUserIds;
    }

    @Override
    public long getSenderUserId() {
        return uploaderUserId;
    }
}
