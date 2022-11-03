package checkmate.goal.application.dto.response;

import checkmate.goal.domain.TeamMate;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TeamMateInfo {
    private long id;
    private long userId;
    private String nickname;
    private boolean uploaded;

    // TODO: 2022/11/03 필요한 필드만
    @QueryProjection @Builder
    public TeamMateInfo(TeamMate teamMate, String nickname) {
        this.id = teamMate.getId();
        this.userId = teamMate.getUserId();
        this.uploaded = teamMate.getUploadable().isUploaded();
        this.nickname = nickname;
    }
}
