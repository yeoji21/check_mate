package checkmate.goal.application.dto.response;

import checkmate.goal.domain.TeamMate;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
                              LocalDate lastUploadDay,
                              String nickname) {
        this.teamMateId = teamMateId;
        this.userId = userId;
        TeamMate teamMate;
        try {
            Constructor<TeamMate> constructor = TeamMate.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            teamMate = constructor.newInstance();

            Field field = TeamMate.class.getDeclaredField("lastUploadDate");
            field.setAccessible(true);
            field.set(teamMate, lastUploadDay);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
        this.uploaded = teamMate.isUploaded();
        this.nickname = nickname;
    }
}
