package checkmate.post.infrastructure;

import checkmate.exception.code.ErrorCode;
import checkmate.exception.RuntimeIOException;
import checkmate.post.domain.FileStore;
import checkmate.post.domain.ImageFileUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class S3FileStore implements FileStore {
    private final AmazonS3 s3;
    @Value("${naver.bucket-name}")
    private String BUCKET_NAME;

    @Override
    public void upload(String storedFilename, String originalFilename, InputStream inputStream) {
        try (inputStream) {
            s3.putObject(BUCKET_NAME, storedFilename, inputStream, ImageFileUtil.getObjectMetadata(originalFilename));
        } catch (IOException e) {
            throw new RuntimeIOException(e, ErrorCode.IMAGE_PROCESSING_IO);
        }
    }

    @Override
    public void delete(String storedName) {
        try {
            s3.deleteObject("checkmate", storedName);
        } catch (AmazonS3Exception e) {
            throw new RuntimeIOException(e, ErrorCode.IMAGE_PROCESSING_IO);
        }
    }
}
