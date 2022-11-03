package checkmate.post.domain;

import checkmate.exception.ExceedImageCountException;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Embeddable
public class Images {
    @OneToMany(mappedBy = "post")
    private final List<Image> images;

    protected Images() {
        this.images = new LinkedList<>();
    }

    List<Image> getImages() {
        return Collections.unmodifiableList(images);
    }

    void putImage(Image image) {
        if(images.size() > 2) throw new ExceedImageCountException();
        images.add(image);
    }
}
