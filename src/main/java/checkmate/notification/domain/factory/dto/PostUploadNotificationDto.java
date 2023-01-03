package checkmate.notification.domain.factory.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PostUploadNotificationDto (
    long uploaderUserId,
    String uploaderNickname,
    long goalId,
    String goalTitle,
    List<Long> teamMateUserIds) implements NotificationCreateDto{
    @Override
    public long getSenderUserId() {
        return uploaderUserId;
    }
}
