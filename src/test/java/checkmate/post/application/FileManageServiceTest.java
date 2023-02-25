package checkmate.post.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.post.domain.FileStore;
import checkmate.post.domain.Image;
import checkmate.post.domain.ImageRepository;
import checkmate.post.domain.Post;
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
    void 파일_업로드_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(mate);
        MockMultipartFile file = new MockMultipartFile("filename", new byte[]{});

        //when
        fileManageService.upload(post, file.getOriginalFilename(), file.getInputStream());

        //then
        verify(fileStore).upload(any(String.class), any(String.class), any(InputStream.class));
        verify(imageRepository).save(any(Image.class));
    }
}