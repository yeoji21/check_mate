package checkmate.post.presentation.dto;

import checkmate.MapperTest;
import checkmate.post.application.dto.request.PostUploadCommand;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

class PostDtoMapperTest extends MapperTest {
    private static final PostDtoMapper mapper = PostDtoMapper.INSTANCE;

    @Test
    void postUploadCommand() throws Exception {
        //given
        List<MultipartFile> files = List.of(new MockMultipartFile("file1", new byte[1]),
                new MockMultipartFile("file2", new byte[1]));
        long userId = 1L;
        PostUploadDto dto = new PostUploadDto(2L, files, "content");

        //when
        PostUploadCommand command = mapper.toCommand(dto, userId);

        //then
        isEqualTo(command.mateId(), dto.getMateId());
        isEqualTo(command.images(), dto.getImages());
        isEqualTo(command.content(), dto.getContent());
    }
}