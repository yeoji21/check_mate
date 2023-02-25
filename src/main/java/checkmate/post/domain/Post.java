package checkmate.post.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.mate.domain.Mate;
import com.mysema.commons.lang.Assert;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @NotNull
    @Column(name = "content")
    private String content;
    @Column(name = "uploaded_date")
    private LocalDate uploadedDate = LocalDate.now();
    @Column(name = "checked")
    private boolean checked = false;
    @Embedded
    private Images images;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id")
    private Mate mate;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Builder
    protected Post(Mate mate, String content) {
        this.mate = mate;
        this.content = content;
        images = new Images();
    }

    public boolean isLikeable() {
        return !uploadedDate.plusDays(1).isBefore(LocalDate.now());
    }

    void addImage(Image image) {
        images.putImage(image);
    }

    public void addLikes(Likes like) {
        if (!isLikeable()) throw new IllegalArgumentException();
        likes.add(like);
        like.setPost(this);
    }

    public void removeLikes(long userId) {
        Assert.isTrue(likes.removeIf(like -> like.getUserId() == userId),
                "userId " + userId + "'s like is not removed");
    }

    public void check() {
        checked = true;
        mate.plusWorkingDay();
    }

    public void uncheck() {
        checked = false;
        mate.minusWorkingDay();
    }

    public List<Image> getImages() {
        return images.getImages();
    }
}
