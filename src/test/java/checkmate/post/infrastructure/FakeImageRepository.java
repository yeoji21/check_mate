package checkmate.post.infrastructure;

import checkmate.post.domain.Image;
import checkmate.post.domain.ImageRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeImageRepository implements ImageRepository {

    private final AtomicLong imageId = new AtomicLong(1);
    private final Map<Long, Image> map = new HashMap<>();

    @Override
    public void save(Image image) {
        ReflectionTestUtils.setField(image, "id", imageId.getAndIncrement());
        map.put(image.getId(), image);
    }
}
