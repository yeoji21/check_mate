package checkmate.goal.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TeamMateUploadInfo {
    private long userId;
    private long teamMateId;
    private String nickname;
    private boolean uploaded;

    @QueryProjection
    @Builder
    public TeamMateUploadInfo(long teamMateId,
                              long userId,
                              LocalDate lastUploadDate,
                              String nickname) {
        this.teamMateId = teamMateId;
        this.userId = userId;
        this.uploaded = lastUploadDate != null && lastUploadDate.isEqual(LocalDate.now());
        this.nickname = nickname;
    }
}
