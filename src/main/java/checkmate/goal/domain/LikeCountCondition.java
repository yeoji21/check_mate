package checkmate.goal.domain;

import checkmate.post.domain.Post;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Getter
@DiscriminatorValue("LIKE_COUNT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LikeCountCondition extends VerificationCondition {
    @Column(name = "minimum_like")
    private int minimumLike;

    public LikeCountCondition(int minimumLike) {
        this.minimumLike = minimumLike;
    }

    @Override
    protected boolean satisfy(Post post) {
        if (post.getCreatedDate().plusDays(1).isBefore(post.getCreatedDate()))
            return false;
        return post.getLikes().size() >= minimumLike;
    }
}
