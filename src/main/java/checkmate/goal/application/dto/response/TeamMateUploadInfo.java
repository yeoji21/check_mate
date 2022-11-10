package checkmate.goal.application.dto.response;

import checkmate.exception.format.BusinessException;
import checkmate.exception.format.ErrorCode;
import checkmate.goal.domain.TeamMate;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Field;
import java.time.LocalDate;

@Getter
public class TeamMateUploadInfo {
    private long id;
    private long userId;
    private String nickname;
    private boolean uploaded;

    @QueryProjection @Builder
    public TeamMateUploadInfo(long teamMateId,
                              long userId,
                              LocalDate lastUploadDay,
                              String nickname) {
        this.id = teamMateId;
        this.userId = userId;
        TeamMate teamMate = new TeamMate(userId);
        try {
            Field field = TeamMate.class.getDeclaredField("lastUploadDay");
            field.setAccessible(true);
            field.set(teamMate, lastUploadDay);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        this.uploaded = teamMate.isUploaded();
        this.nickname = nickname;
    }
}
