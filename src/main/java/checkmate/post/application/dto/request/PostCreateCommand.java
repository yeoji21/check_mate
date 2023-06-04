package checkmate.post.application.dto.request;

import java.util.List;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record PostCreateCommand(
    long userId,
    long mateId,
    List<MultipartFile> images,
    String content) {

}
