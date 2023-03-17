package checkmate.post.domain;

import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class ImageFileUtil {
    public static ObjectMetadata createObjectMetadata(String filename) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setHeader("x-amz-acl", "public-read");
        objectMetadata.setContentType("image/" + getFileExt(filename));
        return objectMetadata;
    }

    private static String getFileExt(String imageName) {
        String[] split = imageName.split("\\.");
        return split[split.length - 1];
    }


    public static String createObjectNameByUUID(String originalFileName) {
        return UUID.randomUUID() + originalFileName;
    }
}
