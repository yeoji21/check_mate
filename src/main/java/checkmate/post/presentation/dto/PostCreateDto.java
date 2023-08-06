package checkmate.post.presentation.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class PostCreateDto {

    private Long mateId;
    private String content;
    private List<MultipartFile> images;

    public PostCreateDto(Long mateId, List<MultipartFile> images, String content) {
        this.mateId = mateId;
        this.content = content;
        this.images = images != null ? new ArrayList<>(images) : Collections.emptyList();
        validateContentAndImageNotEmpty();
    }

    private void validateContentAndImageNotEmpty() {
        if (images.isEmpty() && content.isBlank()) {
            throw new IllegalArgumentException("빈 목표인증 요청");
        }
    }
}
