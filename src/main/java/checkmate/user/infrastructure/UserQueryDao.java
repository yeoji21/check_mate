package checkmate.user.infrastructure;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.types.ExpressionUtils.count;

import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.user.application.dto.DailySchedule;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

    public boolean isExistsNickname(String nickname) {
        return queryFactory.select(user.id)
            .from(user)
            .where(user.nickname.eq(nickname))
            .fetchOne() != null;
    }

    public List<DailySchedule> findSchedule(long userId, List<LocalDate> dates) {
        throw new UnsupportedOperationException("Unsupported findSchedule");
    }
}
