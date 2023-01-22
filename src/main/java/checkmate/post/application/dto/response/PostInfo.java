package checkmate.post.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class PostInfo {
    private long postId;
    private long teamMateId;
    private String uploaderNickname;
    private LocalDateTime uploadAt;
    private List<String> imageUrls;
    private List<Long> likedUserIds;
    private String content;

    @QueryProjection
    public PostInfo(long postId,
                    long teamMateId,
                    String uploaderNickname,
                    LocalDateTime uploadAt,
                    List<String> imageUrls,
                    String content,
                    List<Long> likedUserIds) {
        this.postId = postId;
        this.teamMateId = teamMateId;
        this.uploaderNickname = uploaderNickname;
        this.uploadAt = uploadAt;
        this.imageUrls = imageUrls;
        this.content = content;
        this.likedUserIds = likedUserIds;
    }
}
