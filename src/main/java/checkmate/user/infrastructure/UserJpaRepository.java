package checkmate.user.infrastructure;

import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static checkmate.user.domain.QUser.user;

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
    public Optional<User> findByIdentifier(String identifier) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.identifier.eq(identifier))
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
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }

    @Override
    public void delete(User user) {
        entityManager.remove(user);
    }
}
