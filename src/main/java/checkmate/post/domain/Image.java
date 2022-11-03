package checkmate.post.domain;

import checkmate.common.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;


@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Image extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private long id;
    private String originalName;
    private String storedName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    protected Image(Post post, String originalName, String storedName) {
        this.post = post;
        this.originalName = originalName;
        this.storedName = storedName;
        post.addImage(this);
    }
}
