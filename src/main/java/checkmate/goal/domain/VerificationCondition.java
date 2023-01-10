package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.post.domain.Post;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="dtype")
@Table(
        name = "goal_verification_condition",
        uniqueConstraints = @UniqueConstraint(columnNames = {"goal_id", "dtype"})
)
@Entity
public abstract class VerificationCondition extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", unique = true, nullable = false)
    private Goal goal;

    protected abstract boolean satisfy(Post post);

    public Long getId() {
        return id;
    }

    void setGoal(Goal goal) {
        this.goal = goal;
    }
}