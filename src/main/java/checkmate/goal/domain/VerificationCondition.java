package checkmate.goal.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.post.domain.Post;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Table(
    name = "goal_verification_condition",
    uniqueConstraints = @UniqueConstraint(columnNames = {"goal_id", "dtype"})
)
@Entity
public abstract class VerificationCondition extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", unique = true, nullable = false)
    private Goal goal;

    protected VerificationCondition(Goal goal) {
        this.goal = goal;
    }

    public abstract boolean satisfy(Post post);

    public Long getId() {
        return id;
    }

    public Long getGoalId() {
        return goal.getId();
    }
}