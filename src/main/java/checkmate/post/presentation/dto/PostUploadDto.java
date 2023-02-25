package checkmate.post.presentation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostUploadDto {
    private Long mateId;
    private String content;
    private List<MultipartFile> images;

    public PostUploadDto(Long mateId, List<MultipartFile> images, String content) {
        this.mateId = mateId;
        this.content = content;
        this.images = images != null ? new ArrayList<>(images) : Collections.emptyList();
    }
}
