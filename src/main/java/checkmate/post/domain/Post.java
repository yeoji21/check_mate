package checkmate.post.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
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
    protected Post(Mate mate, String content) {
        this.mate = mate;
        this.content = content;
        images = new Images();
        mate.updatePostUploadedDate();
    }

    void addImage(Image image) {
        images.putImage(image);
    }

    public void addLikes(long userId) {
        checkLikesUpdatable();
        checkAlreadyLiked(userId);
        createNewLike(userId);
    }

    public void removeLikes(long userId) {
        checkLikesUpdatable();
        removeLiked(userId);
    }

    void checkLikesUpdatable() {
        if (createdDate.plusDays(1).isBefore(LocalDate.now()))
            throw new BusinessException(ErrorCode.POST_LIKES_UPDATE);
    }

    private void createNewLike(long userId) {
        Likes likes = new Likes(userId);
        this.likes.add(likes);
        likes.setPost(this);
    }

    private void removeLiked(long userId) {
        boolean isRemoved = likes.removeIf(like -> like.getUserId() == userId);
        if (!isRemoved) throw new BusinessException(ErrorCode.POST_LIKES_UPDATE);
    }

    public void updateCheckStatus() {
        boolean verified = verifyGoalConditions();
        if (!checked && verified) check();
        else if (checked && !verified) uncheck();
    }

    private void checkAlreadyLiked(long userId) {
        boolean exist = likes.stream().anyMatch(like -> like.getUserId() == userId);
        if (exist) throw new BusinessException(ErrorCode.POST_LIKES_UPDATE);
    }

    private boolean verifyGoalConditions() {
        return mate.getGoal().checkConditions(this);
    }

    private void check() {
        checked = true;
        mate.plusWorkingDay();
    }

    private void uncheck() {
        checked = false;
        mate.minusWorkingDay();
    }
}
