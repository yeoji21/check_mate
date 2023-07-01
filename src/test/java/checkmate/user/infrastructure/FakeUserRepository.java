package checkmate.user.infrastructure;

import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeUserRepository implements UserRepository {

    private final AtomicLong userId = new AtomicLong(1);
    private final Map<Long, User> map = new HashMap<>();

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(map.get(userId));
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return map.values().stream()
            .filter(user -> user.getNickname().equals(nickname))
            .findAny();
    }

    @Override
    public Optional<String> findNicknameById(long userId) {
        return Optional.ofNullable(findById(userId).orElse(null).getNickname());
    }

    @Override
    public User save(User user) {
        ReflectionTestUtils.setField(user, "id", userId.getAndIncrement());
        map.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(User user) {
        map.remove(user.getId());
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        return map.values().stream()
            .filter(user -> user.getIdentifier().equals(identifier))
            .findAny();
    }
}
