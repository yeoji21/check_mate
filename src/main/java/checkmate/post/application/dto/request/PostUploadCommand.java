package checkmate.post.application.dto.request;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record PostUploadCommand(
        long userId,
        long mateId,
        List<MultipartFile> images,
        String content) {
}
