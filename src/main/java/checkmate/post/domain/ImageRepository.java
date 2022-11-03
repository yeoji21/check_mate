package checkmate.post.domain;

import java.util.List;

public interface ImageRepository {
    void save(Image image);
    List<Image> findAllByUserId(long userId);
}
