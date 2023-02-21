package checkmate;

import checkmate.config.jwt.JwtFactory;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
public class TestUserGenerate {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JwtFactory jwtFactory;


    @Test
    @Transactional
    @Rollback(value = false)
    void test() throws Exception {
        User user = TestEntityFactory.user(null, "user");
        entityManager.persist(user);

        String accessToken = jwtFactory.accessToken(user);

        System.out.println("=====================================");
        System.out.println(accessToken);
        System.out.println("=====================================");
    }
}
