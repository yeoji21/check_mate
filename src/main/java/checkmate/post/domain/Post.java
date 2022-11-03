package checkmate.post.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.goal.domain.TeamMate;
import com.mysema.commons.lang.Assert;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private long id;
    @Column(name="text_data")
    private String text;
    @Column(name = "uploaded_date")
    private LocalDate uploadedDate = LocalDate.now();
    @Column(name = "is_checked")
    private boolean isChecked = false;
    @Embedded
    private Images images;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_mate_id")
    private TeamMate teamMate;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Builder
    protected Post(TeamMate teamMate, String text) {
        this.teamMate = teamMate;
        this.text = text;
        images = new Images();
    }

    public boolean isLikeable() {
        return ! uploadedDate.plusDays(1).isBefore(LocalDate.now());
    }

    void addImage(Image image) {
        images.putImage(image);
    }

    public void addLikes(Likes like) {
        if(!isLikeable()) throw new IllegalArgumentException();
        likes.add(like);
        like.setPost(this);
    }

    public void removeLikes(long userId) {
        Assert.isTrue(likes.removeIf(like -> like.getUserId() == userId),
                "userId " + userId + "'s like is not removed");
    }

    public void check() {
        isChecked = true;
        teamMate.plusWorkingDay();
    }

    public void uncheck() {
        isChecked = false;
        teamMate.minusWorkingDay();
    }

    public List<Image> getImages() {
        return images.getImages();
    }
}
