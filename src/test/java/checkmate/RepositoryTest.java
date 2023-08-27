package checkmate;

import checkmate.config.jpa.ApplicationAuditingConfig;
import checkmate.config.jpa.JpaQueryFactoryConfig;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.GoalJpaRepository;
import checkmate.goal.infra.GoalQueryDao;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateJpaRepository;
import checkmate.mate.infra.MateQueryDao;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.infrastructure.NotificationJpaRepository;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.post.domain.ImageRepository;
import checkmate.post.domain.PostRepository;
import checkmate.post.infrastructure.ImageJpaRepository;
import checkmate.post.infrastructure.PostJpaRepository;
import checkmate.post.infrastructure.PostQueryDao;
import checkmate.user.infrastructure.UserJpaRepository;
import checkmate.user.infrastructure.UserQueryDao;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Import({ApplicationAuditingConfig.class, JpaQueryFactoryConfig.class,
    GoalQueryDao.class,
    MateJpaRepository.class,
    MateQueryDao.class,
    NotificationJpaRepository.class,
    ImageJpaRepository.class,
    PostJpaRepository.class,
    GoalJpaRepository.class,
    PostJpaRepository.class,
    PostQueryDao.class,
    NotificationQueryDao.class,
    UserJpaRepository.class,
    UserQueryDao.class
})
@ExtendWith(SpringExtension.class)
@DataJpaTest
public abstract class RepositoryTest {

    @Autowired
    protected EntityManager em;
    @Autowired
    protected JPAQueryFactory queryFactory;
    @Autowired
    protected GoalRepository goalRepository;
    @Autowired
    protected MateRepository mateRepository;
    @Autowired
    protected NotificationRepository notificationRepository;
    @Autowired
    protected ImageRepository imageRepository;
    @Autowired
    protected PostRepository postRepository;
    @Autowired
    protected GoalQueryDao goalQueryDao;
    @Autowired
    protected MateQueryDao mateQueryDao;
    @Autowired
    protected PostQueryDao postQueryDao;
    @Autowired
    protected NotificationQueryDao notificationQueryDao;
    @Autowired
    protected UserJpaRepository userRepository;
    @Autowired
    protected UserQueryDao userQueryDao;
}
