package checkmate.post.domain;

import java.io.InputStream;

public interface FileStore {
    void upload(String storedName, String imageName, InputStream inputStream);

    void delete(String storedName);
}
