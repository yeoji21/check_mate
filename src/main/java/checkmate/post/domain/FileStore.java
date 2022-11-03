package checkmate.post.domain;

import java.io.InputStream;

public interface FileStore {
    void upload(String test, String imageName, InputStream inputStream);
    void delete(String storedName);
}
