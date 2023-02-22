package checkmate.post.application.dto.request;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record PostUploadCommand(
        long userId,
        long teamMateId,
        List<MultipartFile> images,
        String content) {
}
