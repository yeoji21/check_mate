package checkmate.mate.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MateUploadInfo {
    private long userId;
    private long mateId;
    private String nickname;
    private boolean uploaded;

    @QueryProjection
    @Builder
    public MateUploadInfo(long mateId,
                          long userId,
                          LocalDate lastUploadDate,
                          String nickname) {
        this.mateId = mateId;
        this.userId = userId;
        this.uploaded = lastUploadDate != null && lastUploadDate.isEqual(LocalDate.now());
        this.nickname = nickname;
    }
}
