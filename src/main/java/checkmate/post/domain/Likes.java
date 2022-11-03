package checkmate.post.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "user_post", columnNames = {"user_id", "post_id"})
})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Likes {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private long id;
    @Column(name = "user_id")
    private long userId;
    @NotNull @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    public Likes(long userId) {
        this.userId = userId;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
