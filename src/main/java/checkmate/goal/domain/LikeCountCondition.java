package checkmate.goal.domain;

import checkmate.post.domain.Post;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@DiscriminatorValue("LIKE_COUNT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LikeCountCondition extends VerificationCondition {

    @Column(name = "minimum_like")
    private int minimumLike;

    public LikeCountCondition(Goal goal, int minimumLike) {
        super(goal);
        this.minimumLike = minimumLike;
    }

    @Override
    public boolean satisfy(Post post) {
        if (isPastThanYesterDay(post.getCreatedDate())) {
            return false;
        }
        return post.getLikes().size() >= minimumLike;
    }

    private boolean isPastThanYesterDay(LocalDate date) {
        return date.isBefore(LocalDate.now().minusDays(1));
    }
}
