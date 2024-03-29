package checkmate.post.application;

import checkmate.post.domain.FileStore;
import checkmate.post.domain.Image;
import checkmate.post.domain.ImageFileUtil;
import checkmate.post.domain.ImageRepository;
import checkmate.post.domain.Post;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FileManageService {

    private final ImageRepository imageRepository;
    private final FileStore fileStore;

    public void upload(Post post, String imageName, InputStream inputStream) {
        String storedFilename = ImageFileUtil.createObjectNameByUUID(imageName);
        fileStore.upload(storedFilename, imageName, inputStream);
        saveImage(post, imageName, storedFilename);
    }

    private void saveImage(Post post, String imageName, String storedFilename) {
        Image image = Image.builder()
            .post(post)
            .originalName(imageName)
            .storedName(storedFilename)
            .build();
        imageRepository.save(image);
    }
}
