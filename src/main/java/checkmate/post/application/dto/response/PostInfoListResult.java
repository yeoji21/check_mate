package checkmate.post.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostInfoListResult {
    private String goalTitle;
    private List<PostInfo> posts;

    @Builder
    public PostInfoListResult(String goalTitle, List<PostInfo> posts) {
        this.goalTitle = goalTitle;
        this.posts = posts;
    }
}
