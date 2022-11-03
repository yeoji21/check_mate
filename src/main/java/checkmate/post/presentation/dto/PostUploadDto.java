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
    private Long teamMateId;
    private String text;
    private List<MultipartFile> images;

    public PostUploadDto(Long teamMateId, List<MultipartFile> images, String text) {
        this.teamMateId = teamMateId;
        this.text = text;
        this.images = images != null ? new ArrayList<>(images) : Collections.emptyList();
    }
}
