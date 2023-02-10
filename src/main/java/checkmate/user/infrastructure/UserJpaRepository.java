package checkmate.user.infrastructure;

import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static checkmate.user.domain.QUser.user;

// TODO: 2023/02/11 무의미한 DAO 합성 구조 개선
@RequiredArgsConstructor
@Repository
public class UserJpaRepository implements UserRepository {
    private final UserDao userDao;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<User> findById(long userId) {
        return userDao.findById(userId);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return userDao.findByNickname(nickname);
    }

    @Override
    public Optional<User> findByProviderId(String providerId) {
        return userDao.findByProviderId(providerId);
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
        return userDao.save(user);
    }
}
