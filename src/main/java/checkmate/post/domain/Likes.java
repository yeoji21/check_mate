package checkmate.post.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(name = "user_post", columnNames = {"user_id", "post_id"}),
        indexes = {
                @Index(name = "postId_userId_idx", columnList = "post_id, user_id")
        })
@Entity
public class Likes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private long id;
    @Column(name = "user_id")
    private long userId;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    public Likes(long userId) {
        this.userId = userId;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
