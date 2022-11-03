package checkmate.post.application.dto.request;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PostUploadCommand {
    private long teamMateId;
    private List<MultipartFile> images;
    private String text;

    @Builder
    public PostUploadCommand(long teamMateId,
                             List<MultipartFile> images,
                             String text) {
        this.teamMateId = teamMateId;
        this.images = new ArrayList<>(images);
        this.text = text;
    }
}
