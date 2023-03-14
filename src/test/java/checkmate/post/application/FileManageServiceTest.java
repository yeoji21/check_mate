package checkmate.post.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.post.domain.FileStore;
import checkmate.post.domain.Image;
import checkmate.post.domain.ImageRepository;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileManageServiceTest {
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private FileStore fileStore;
    @InjectMocks
    private FileManageService fileManageService;

    @Test
    @DisplayName("파일 업로드")
    void upload() throws Exception {
        //given
        Post post = createPost();
        MockMultipartFile file = createMultipartFile();

        //when
        fileManageService.upload(post, file.getOriginalFilename(), file.getInputStream());

        //then
        verify(fileStore).upload(any(String.class), any(String.class), any(InputStream.class));
        verify(imageRepository).save(any(Image.class));
    }

    private MockMultipartFile createMultipartFile() {
        return new MockMultipartFile("filename", new byte[]{});
    }

    private Post createPost() {
        Mate mate = createMate();
        return TestEntityFactory.post(mate);
    }

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(1L, "test");
        return goal.join(TestEntityFactory.user(1L, "user"));
    }
}