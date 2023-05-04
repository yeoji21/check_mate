package checkmate.notification.domain.factory.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostUploadNotificationDto implements NotificationCreateDto {
    private long uploaderUserId;
    private String uploaderNickname;
    private long goalId;
    private String goalTitle;
    private List<Long> mateUserIds;

    @QueryProjection
    @Builder
    public PostUploadNotificationDto(long uploaderUserId,
                                     String uploaderNickname,
                                     long goalId,
                                     String goalTitle) {
        this.uploaderUserId = uploaderUserId;
        this.uploaderNickname = uploaderNickname;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
    }

    public void setMateUserIds(List<Long> mateUserIds) {
        this.mateUserIds = mateUserIds;
    }

    @Override
    public long getSenderUserId() {
        return uploaderUserId;
    }
}
