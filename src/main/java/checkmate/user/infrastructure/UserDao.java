package checkmate.user.infrastructure;

import checkmate.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByProviderId(String providerId);
}
