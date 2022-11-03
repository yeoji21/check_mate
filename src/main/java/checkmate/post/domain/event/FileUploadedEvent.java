package checkmate.post.domain.event;

import checkmate.post.domain.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@Getter
@RequiredArgsConstructor
public class FileUploadedEvent {
    private final Post post;
    private final String imageName;
    private final InputStream inputStream;
}
