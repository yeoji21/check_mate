package checkmate.post.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageNameDto {
    private final String originalName;
    private final String storedName;

    @Builder
    ImageNameDto(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }
}
