package checkmate.user.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTest {
    @Test
    void findNicknameById() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);

        em.flush();
        em.clear();

        //when
        String nickname = userJpaRepository.findNicknameById(user.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(nickname).isEqualTo(user.getNickname());
    }
}