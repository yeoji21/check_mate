package checkmate.post.domain;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ImageFileUtilTest {
    @Test
    @DisplayName("ObjectMetadata 생성")
    void createObjectMetadata() throws Exception {
        //given
        MockMultipartFile jpgFile = createMultipartFile("originalFileName.jpg", "image/jpg");
        MockMultipartFile pngFile = createMultipartFile("originalFileName.png", "image/png");
        MockMultipartFile jpegFile = createMultipartFile("originalFileName.jpeg", "image/jpeg");

        //when
        ObjectMetadata jpgObject = ImageFileUtil.createObjectMetadata(jpgFile.getOriginalFilename());
        ObjectMetadata pngObject = ImageFileUtil.createObjectMetadata(pngFile.getOriginalFilename());
        ObjectMetadata jpegObject = ImageFileUtil.createObjectMetadata(jpegFile.getOriginalFilename());

        //then
        assertThat(jpgObject.getContentType()).isEqualTo("image/jpg");
        assertThat(pngObject.getContentType()).isEqualTo("image/png");
        assertThat(jpegObject.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("UUID 랜덤 이름 생성")
    void createObjectNameByUUID() throws Exception {
        //given
        MockMultipartFile multipartFile = createMultipartFile("originalFileName.jpg", "image/jpg");

        //when
        String storedName = ImageFileUtil.createObjectNameByUUID(multipartFile.getOriginalFilename());

        //then
        assertThat(storedName).contains(multipartFile.getOriginalFilename());
    }

    private MockMultipartFile createMultipartFile(String originalFilename, String contentType) throws IOException {
        return new MockMultipartFile("multipartFileName", originalFilename, contentType, InputStream.nullInputStream());
    }
}