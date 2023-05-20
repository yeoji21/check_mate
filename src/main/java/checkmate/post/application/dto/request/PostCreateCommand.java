package checkmate.post.application.dto.request;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// TODO: 2023/05/21 Create로 이름 변경 고려
@Builder
public record PostCreateCommand(
        long userId,
        long mateId,
        List<MultipartFile> images,
        String content) {
}
