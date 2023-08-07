package checkmate.post.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post", indexes = {
    @Index(name = "mateId_idx", columnList = "mate_id"),
    @Index(name = "createdDate_idx", columnList = "created_date")
})
@Entity
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @NotNull
    @Column(name = "content")
    private String content;
    @Embedded
    private Images images;
    @Column(name = "checked")
    private boolean checked = false;
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id")
    private Mate mate;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Builder
    public Post(Mate mate, String content) {
        this.mate = mate;
        this.content = content;
        images = new Images();
        mate.updateLastUpdateDate();
    }

    void addImage(Image image) {
        images.putImage(image);
    }

    public void addLikes(long userId) {
        checkLikesUpdatable();
        checkAlreadyLiked(userId);
        createLikes(userId);
    }

    public void removeLikes(long userId) {
        checkLikesUpdatable();
        if (!likes.removeIf(like -> like.getUserId() == userId)) {
            throw new BusinessException(ErrorCode.POST_LIKES_UPDATE);
        }
    }

    private void checkLikesUpdatable() {
        if (createdDate.plusDays(1).isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.POST_LIKES_UPDATE);
        }
    }

    private void createLikes(long userId) {
        Likes likes = new Likes(userId);
        this.likes.add(likes);
        likes.setPost(this);
    }

    public void updateCheckStatus() {
        boolean verified = verifyGoalConditions();
        if (!checked && verified) {
            check();
        } else if (checked && !verified) {
            uncheck();
        }
    }

    private void checkAlreadyLiked(long userId) {
        boolean exist = likes.stream().anyMatch(like -> like.getUserId() == userId);
        if (exist) {
            throw new BusinessException(ErrorCode.POST_LIKES_UPDATE);
        }
    }

    private boolean verifyGoalConditions() {
        return mate.getGoal().checkConditions(this);
    }

    public void check() {
        checked = true;
        mate.plusCheckDayCount();
    }

    public void uncheck() {
        checked = false;
        mate.minusCheckDayCount();
    }
}
