package checkmate.post.domain;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ImageFileUtilTest {

    @Test
    void MultipartFile로부터_확장자_추출_테스트() throws Exception{
        //given
        MockMultipartFile multipartFile = new MockMultipartFile("multipartFileName", "originalFileName.jpg", "image/jpg", InputStream.nullInputStream());
        //when
        String ext = ImageFileUtil.getFileExt(multipartFile.getOriginalFilename());
        //then
        assertThat(ext).isEqualTo("jpg");
    }

    @Test
    void 확장자를_통해_ObjectMetadata_생성_테스트() throws Exception{
        //given
        String ext = "jpg";
        //when
        ObjectMetadata objectMetadata = ImageFileUtil.getObjectMetadata(ext);
        //then
        assertThat(objectMetadata.getContentType()).isEqualTo("image/" + ext);
    }

    @Test
    void UUID_랜덤_저장_이름_생성_테스트() throws Exception{
        //given
        MockMultipartFile multipartFile = new MockMultipartFile("multipartFileName", "originalFileName.jpg", "image/jpg", InputStream.nullInputStream());
        //when
        String storedName = ImageFileUtil.getObjectNameByUUID(multipartFile.getOriginalFilename());
        //then
        System.out.println(storedName);
        assertThat(storedName).contains("originalFileName.jpg");
    }
}