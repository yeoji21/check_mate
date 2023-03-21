package checkmate.user.infrastructure;

import checkmate.goal.domain.GoalStatus;
import checkmate.mate.domain.MateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static com.querydsl.core.types.ExpressionUtils.count;

@RequiredArgsConstructor
@Repository
public class UserQueryDao {
    private final JPAQueryFactory queryFactory;

    public int countOngoingGoals(long userId) {
        return queryFactory.select(count(goal.id))
                .from(mate)
                .join(mate.goal, goal)
                .where(mate.userId.eq(userId),
                        mate.status.eq(MateStatus.ONGOING),
                        goal.status.eq(GoalStatus.ONGOING))
                .fetchOne()
                .intValue();
    }
}
