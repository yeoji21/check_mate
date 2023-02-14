package checkmate.user.infrastructure;

import checkmate.goal.domain.GoalStatus;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.types.ExpressionUtils.count;

@RequiredArgsConstructor
@Repository
public class UserJpaRepository implements UserRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.id.eq(userId))
                        .fetchOne()
        );
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.nickname.eq(nickname))
                        .fetchOne()
        );
    }


    @Override
    public Optional<User> findByProviderId(String providerId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.providerId.eq(providerId))
                        .fetchOne()
        );
    }

    @Override
    public Optional<String> findNicknameById(long userId) {
        return Optional.ofNullable(
                queryFactory.select(user.nickname)
                        .from(user)
                        .where(user.id.eq(userId))
                        .fetchOne()
        );
    }

    @Override
    public int countOngoingGoals(long userId) {
        return queryFactory.select(count(goal.id))
                .from(teamMate)
                .join(teamMate.goal, goal)
                .where(teamMate.userId.eq(userId),
                        teamMate.status.eq(TeamMateStatus.ONGOING),
                        goal.status.eq(GoalStatus.ONGOING))
                .fetchOne()
                .intValue();
    }

    @Override
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }
}
